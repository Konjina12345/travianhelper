package com.energodu.travianhelper

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import org.json.JSONArray
import org.json.JSONTokener

/** Read-only Android village navigation prototype. Login remains manual in WebView. */
class MainActivity : Activity() {
    private lateinit var web: WebView
    private lateinit var address: EditText
    private lateinit var villageDid: EditText
    private lateinit var status: TextView
    private lateinit var results: TextView
    private val handler = Handler(Looper.getMainLooper())
    private var navigationAttempts = 0
    private var consecutiveMatches = 0
    private var expectedDid = ""
    private var requestedView = ""
    private var scanVillages = JSONArray()
    private var scanIndex = 0
    private var scanRunning = false
    private var scanPreparedForSwitch = false
    private val scanResults = mutableListOf<String>()
    private var scanResourceSummary = ""
    private val villageCache by lazy { getSharedPreferences("travian_village_cache", MODE_PRIVATE) }

    private fun cachedResources(did: String): JSONArray? {
        return try {
            val arr = JSONArray(villageCache.getString("resources_$did", null) ?: return null)
            val levels = mutableMapOf<Int, Int>()
            for (i in 0 until arr.length()) {
                val o = arr.optJSONObject(i) ?: continue
                val slot = o.optInt("slot")
                if (slot in 1..18) levels[slot] = o.optInt("level")
            }
            if (levels.size == 18 && (1..18).all { (levels[it] ?: 0) >= 10 }) arr else null
        } catch (_: Exception) { null }
    }

    private fun saveResourceCache(did: String, arr: JSONArray): Boolean {
        return try {
            val levels = mutableMapOf<Int, Int>()
            for (i in 0 until arr.length()) {
                val o = arr.optJSONObject(i) ?: continue
                val slot = o.optInt("slot")
                if (slot in 1..18) levels[slot] = o.optInt("level")
            }
            if (levels.size != 18) return false
            villageCache.edit().putString("resources_$did", arr.toString()).apply()
            true
        } catch (_: Exception) { false }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(10, 8, 10, 8) }
        address = EditText(this).apply { hint = "Travian server ili login link (https://...)"; setMaxLines(1); setText("https://") }
        root.addView(address, LinearLayout.LayoutParams(-1, ViewGroup.LayoutParams.WRAP_CONTENT))
        root.addView(makeButton("Otvori Travian / prijava") { openServer() })
        val controls = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        controls.addView(makeButton("Provjeri selo") { readVillage() }, LinearLayout.LayoutParams(0, -2, 1f))
        controls.addView(makeButton("Resursi") { navigateView("resource") }, LinearLayout.LayoutParams(0, -2, 1f))
        controls.addView(makeButton("Centar") { navigateView("center") }, LinearLayout.LayoutParams(0, -2, 1f))
        root.addView(controls)
        val villageRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        villageDid = EditText(this).apply { hint = "ID sela (DID)"; setMaxLines(1) }
        villageRow.addView(villageDid, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        villageRow.addView(makeButton("Promijeni selo") {
            val did = villageDid.text.toString().trim()
            if (did.isNotEmpty()) switchToVillage(did, "center") else Toast.makeText(this, "Unesi DID sela", Toast.LENGTH_SHORT).show()
        })
        root.addView(villageRow)
        val scanRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        scanRow.addView(makeButton("UČITAJ SVA SELA") { startScanAllVillages() }, LinearLayout.LayoutParams(0, -2, 2f))
        scanRow.addView(makeButton("ZAUSTAVI") { stopScan() }, LinearLayout.LayoutParams(0, -2, 1f))
        root.addView(scanRow)
        status = TextView(this).apply { text = "Spremno. Prijavi se u Travian kroz prozor ispod."; textSize = 13f; setPadding(4, 8, 4, 4) }
        root.addView(status)
        results = TextView(this).apply { text = "Rezultati učitavanja sela će se prikazati ovdje."; textSize = 12f; setPadding(4, 4, 4, 8) }
        root.addView(results)
        web = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.loadsImagesAutomatically = true
            settings.javaScriptCanOpenWindowsAutomatically = false
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    setStatus("Stranica učitana: ${url ?: ""}")
                }
            }
        }
        root.addView(web, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)
    }

    private fun makeButton(label: String, action: () -> Unit) = Button(this).apply { text = label; setOnClickListener { action() } }

    private fun openServer() {
        var url = address.text.toString().trim()
        if (url.isBlank()) return
        if (!url.startsWith("https://") && !url.startsWith("http://")) url = "https://$url"
        if (!url.endsWith("login.php") && !url.contains("/dorf")) url = url.trimEnd('/') + "/login.php"
        web.loadUrl(url)
        setStatus("Otvaram Travian. Prijavu obavi ručno u stranici igre.")
    }

    private fun readVillage() {
        runJs("""(function(){
          const sels=['.villageList .listEntry.village.active[data-did]','#sidebarBoxVillagelist .listEntry.village.active[data-did]','.listEntry.village.active[data-did]'];
          let e=null; for(const s of sels){e=document.querySelector(s); if(e) break;}
          let did=e?.getAttribute('data-did')||'';
          if(!did){const i=document.querySelector("input.villageInput[data-did],input[name='villageName'][data-did]"); did=i?.getAttribute('data-did')||'';}
          const name=e?.querySelector('span.name,.name')?.textContent?.trim()||'';
          return JSON.stringify({did:did,name:name,url:location.href});
        })();""") { result -> setStatus("Podaci sela: $result") }
    }

    private fun navigateView(view: String) {
        runJs("(function(){return location.href;})()") { currentUrl ->
            val target = if (view == "resource") "dorf1.php" else "dorf2.php"
            if (currentUrl.contains(target)) { setStatus("Već je otvoren traženi prikaz ($view)."); return@runJs }
            clickView(view) { ok ->
                if (ok) { setStatus("Otvoren prikaz $view."); handler.postDelayed({ readVillage() }, 1600) }
                else setStatus("Nije pronađeno dugme za prikaz $view.")
            }
        }
    }

    private fun clickView(view: String, done: (Boolean) -> Unit) {
        val target = if (view == "resource") "dorf1.php" else "dorf2.php"
        val selector = if (view == "resource") "a.village.resourceView,a.resourceView,a[href*='dorf1.php']"
                       else "a.village.buildingView,a.buildingView,a[href*='dorf2.php']"
        runJs("(function(){const e=document.querySelector(${jsString(selector)});if(!e)return 'NO';e.click();return 'YES';})()") { r ->
            if (r.contains("YES")) handler.postDelayed({ done(true) }, 1900)
            else runJs("(function(){const u=new URL(location.href);u.pathname=u.pathname.replace(/[^/]+$/,${jsString(target)});location.href=u.toString();return 'FALLBACK';})()") {
                handler.postDelayed({ done(true) }, 2300)
            }
        }
    }

    private fun switchToVillage(did: String, view: String) {
        expectedDid = did; requestedView = view; navigationAttempts = 0; consecutiveMatches = 0
        trySwitchAttempt()
    }

    private fun trySwitchAttempt() {
        if (expectedDid.isBlank()) { setStatus("STOP: DID sela nije zadat."); return }
        if (navigationAttempts >= 15) { setStatus("STOP: DID $expectedDid nije potvrđen poslije 15 pokušaja."); return }
        navigationAttempts++
        val js = """(function(){const did=${jsString(expectedDid)};
          const e=document.querySelector('.villageList .listEntry.village[data-did="'+did+'"],#sidebarBoxVillagelist .listEntry.village[data-did="'+did+'"],.listEntry.village[data-did="'+did+'"]');
          if(!e)return 'NOT_FOUND';const t=e.querySelector('a,span.name,.name')||e;t.scrollIntoView({block:'center'});t.click();return 'CLICKED';})();"""
        runJs(js) { handler.postDelayed({ verifyDidTwice() }, 500) }
    }

    private fun verifyDidTwice() {
        val js = """(function(){const e=document.querySelector('.villageList .listEntry.village.active[data-did],#sidebarBoxVillagelist .listEntry.village.active[data-did],.listEntry.village.active[data-did]');if(e)return e.getAttribute('data-did')||'';const i=document.querySelector('input.villageInput[data-did],input[name="villageName"][data-did]');return i?.getAttribute('data-did')||'';})()"""
        runJs(js) { raw ->
            val did = raw.trim('"')
            if (did == expectedDid) consecutiveMatches++ else consecutiveMatches = 0
            if (consecutiveMatches >= 2) {
                if (scanRunning) scanOpenResource() else { setStatus("Potvrđen DID $expectedDid."); navigateView(requestedView) }
            } else if (navigationAttempts < 15) handler.postDelayed({ trySwitchAttempt() }, 450)
            else setStatus("STOP: selo nije pouzdano potvrđeno. Nema izvršavanja zadatka.")
        }
    }

    // Scan remains read-only: manually log in first, then the app navigates and reads each village.
    private fun startScanAllVillages() {
        if (scanRunning) { setStatus("Učitavanje je već u toku."); return }
        setStatus("Tražim sela u Travian listi…")
        val js = """(function(){
          const all=[...document.querySelectorAll('#sidebarBoxVillagelist .listEntry.village[data-did],.villageList .listEntry.village[data-did],.listEntry.village[data-did]')];
          const seen=new Set(), out=[];
          for(const e of all){const did=e.getAttribute('data-did');if(!did||seen.has(did))continue;seen.add(did);
            const name=e.querySelector('span.name,.name')?.textContent?.trim()||e.textContent.trim().split('\n')[0]||('Selo '+did);
            out.push({did:did,name:name});}
          return JSON.stringify(out);
        })();"""
        runJs(js) { raw ->
            try {
                val decoded = JSONTokener(raw).nextValue() as? String ?: "[]"
                scanVillages = JSONArray(decoded)
                if (scanVillages.length() == 0) { setStatus("Nisam pronašao sela. Provjeri da si prijavljen i da je lista sela učitana."); return@runJs }
                scanIndex = 0; scanResults.clear(); scanRunning = true
                results.text = "Pronađeno sela: ${scanVillages.length()}\nPočinjem redom: resursi → centar."
                scanNextVillage()
            } catch (e: Exception) { setStatus("Greška pri čitanju liste sela: ${e.message}") }
        }
    }

    private fun scanNextVillage() {
        if (!scanRunning) return
        if (scanIndex >= scanVillages.length()) {
            scanRunning = false
            results.text = "ZAVRŠENO: ${scanResults.size}/${scanVillages.length()} sela obrađeno.\n" + scanResults.joinToString("\n")
            setStatus("Učitavanje svih sela završeno.")
            return
        }
        val item = scanVillages.optJSONObject(scanIndex) ?: run { scanIndex++; scanNextVillage(); return }
        expectedDid = item.optString("did"); requestedView = "scan"; navigationAttempts = 0; consecutiveMatches = 0; scanPreparedForSwitch = false
        setStatus("Selo ${scanIndex + 1}/${scanVillages.length()}: ${item.optString("name")} (DID $expectedDid) — prebacujem…")
        scanSwitchAttempt()
    }

    private fun scanSwitchAttempt() {
        if (!scanRunning) return
        if (!scanPreparedForSwitch) {
            runJs("(function(){return location.href;})()") { current ->
                if (current.contains("dorf1.php")) {
                    setStatus("Napustam dorf1 trenutnog sela i vraćam se u centar prije promjene sela…")
                    clickView("center") { ok ->
                        if (!ok) {
                            scanResults.add("GREŠKA: ne mogu vratiti trenutno selo na centar prije promjene")
                            scanRunning = false
                            setStatus("Skeniranje zaustavljeno radi sigurnosti navigacije.")
                        } else {
                            scanPreparedForSwitch = true
                            handler.postDelayed({ scanSwitchAttempt() }, 1700)
                        }
                    }
                } else {
                    scanPreparedForSwitch = true
                    scanSwitchAttempt()
                }
            }
            return
        }
        if (navigationAttempts >= 15) { scanResults.add("GREŠKA DID $expectedDid — nije potvrđen"); scanIndex++; scanNextVillage(); return }
        navigationAttempts++
        val js = """(function(){const did=${jsString(expectedDid)};
          const e=document.querySelector('#sidebarBoxVillagelist .listEntry.village[data-did="'+did+'"],.villageList .listEntry.village[data-did="'+did+'"],.listEntry.village[data-did="'+did+'"]');
          if(!e)return 'NOT_FOUND';const t=e.querySelector('a,span.name,.name')||e;t.scrollIntoView({block:'center'});t.click();return 'CLICKED';})();"""
        runJs(js) { handler.postDelayed({ scanVerifyDid() }, 650) }
    }

    private fun scanVerifyDid() {
        if (!scanRunning) return
        val js = """(function(){const e=document.querySelector('#sidebarBoxVillagelist .listEntry.village.active[data-did],.villageList .listEntry.village.active[data-did],.listEntry.village.active[data-did]');return e?.getAttribute('data-did')||'';})()"""
        runJs(js) { raw ->
            if (raw.trim('"') == expectedDid) consecutiveMatches++ else consecutiveMatches = 0
            if (consecutiveMatches >= 2) {
                setStatus("DID $expectedDid potvrđen — provjeravam keš resursa…")
                scanChooseFirstView()
            } else if (navigationAttempts < 15) handler.postDelayed({ scanSwitchAttempt() }, 450)
            else { scanResults.add("GREŠKA DID $expectedDid — potvrda nije uspjela"); scanIndex++; scanNextVillage() }
        }
    }

    private fun scanChooseFirstView() {
        if (!scanRunning) return
        if (cachedResources(expectedDid) != null) {
            scanResourceSummary = "KEŠ: svih 18 polja je potvrđeno na L10+"
            setStatus("DID $expectedDid — svih 18 polja L10+; preskačem dorf1 i idem direktno u centar.")
            scanOpenCenter()
        } else {
            setStatus("DID $expectedDid — keš nije kompletan; učitavam dorf1.")
            scanOpenResource()
        }
    }
    private fun scanOpenResource() {
        if (!scanRunning) return
        runJs("(function(){return location.href;})()") { url ->
            if (url.contains("dorf1.php")) captureResourcePage()
            else clickView("resource") { ok ->
                if (!ok) { scanResults.add("GREŠKA DID $expectedDid — resursna stranica nije otvorena"); scanIndex++; scanNextVillage() }
                else captureResourcePage()
            }
        }
    }
    private fun scanOpenCenter() {
        if (!scanRunning) return
        runJs("(function(){return location.href;})()") { url ->
            if (url.contains("dorf2.php")) captureCenterPage()
            else clickView("center") { ok ->
                if (!ok) { scanResults.add("GREŠKA DID $expectedDid — centar nije otvoren"); scanIndex++; scanNextVillage() }
                else captureCenterPage()
            }
        }
    }

    private fun captureResourcePage() {
        if (!scanRunning) return
        val js = """(function(){
          const out=[], seen=new Set();
          let anchors=[...document.querySelectorAll('a.resourceField')];
          if(!anchors.length) anchors=[...document.querySelectorAll("a[href*='build.php?id=']")];
          for(const a of anchors){
            const m=(a.getAttribute('href')||'').match(/[?&]id=(\d+)/);
            if(!m) continue;
            const slot=parseInt(m[1],10);
            if(slot<1||slot>18||seen.has(slot)) continue;
            const levelEl=a.querySelector('span.level,[data-level]');
            let level=parseInt(levelEl?.getAttribute('data-level')||'',10);
            if(!Number.isFinite(level)){
              const t=(levelEl?.innerText||a.innerText||a.title||'');
              const lm=t.match(/(?:level|lvl|nivo|stufe)?\s*[:#]?\s*(\d+)/i);
              level=lm?parseInt(lm[1],10):0;
            }
            const gm=(a.getAttribute('class')||'').match(/gid(\d+)/);
            out.push({slot:slot,level:Number.isFinite(level)?level:0,gid:gm?gm[1]:'',name:(a.title||a.innerText||'').trim()});
            seen.add(slot);
          }
          out.sort((a,b)=>a.slot-b.slot);
          return JSON.stringify({fields:out});
        })();"""
        runJs(js) { raw ->
            try {
                val decoded = JSONTokener(raw).nextValue() as? String ?: "{}"
                val fields = org.json.JSONObject(decoded).optJSONArray("fields") ?: JSONArray()
                if (fields.length() != 18) {
                    scanResults.add("GREŠKA DID $expectedDid — resursni DOM \${fields.length()}/18; nepotpun keš nije sačuvan")
                    scanIndex++
                    scanNextVillage()
                    return@runJs
                }
                val saved = saveResourceCache(expectedDid, fields)
                scanResourceSummary = "DORF1: 18/18 polja; keš \${if (saved) "sačuvan" else "nije potvrđen"}"
                setStatus("DID $expectedDid — resursi očitani 18/18; prelazim direktno u centar.")
                scanOpenCenter()
            } catch (e: Exception) {
                scanResults.add("GREŠKA DID $expectedDid — parsiranje resursa: \${e.message}")
                scanIndex++
                scanNextVillage()
            }
        }
    }

    private fun captureCenterPage() {
        if (!scanRunning) return
        val js = """(function(){
          const slots=[...document.querySelectorAll('#villageContent div.buildingSlot[data-aid],div.buildingSlot[data-aid]')];
          const seen=new Set(), out=[];
          for(const e of slots){
            const slot=parseInt(e.getAttribute('data-aid')||'0',10);
            if(!slot||slot<19||seen.has(slot)) continue;
            seen.add(slot);
            const gid=e.getAttribute('data-gid')||'';
            const name=e.getAttribute('data-name')||(gid==='0'?'Prazno polje':('GID '+gid));
            const a=e.querySelector('a.level,a[data-level],a[href*="build.php?id="]');
            let level=parseInt(a?.getAttribute('data-level')||'',10);
            if(!Number.isFinite(level)){
              const m=(a?.innerText||a?.title||'').match(/\d+/);
              level=m?parseInt(m[0],10):0;
            }
            out.push({slot:slot,gid:gid,name:name,level:Number.isFinite(level)?level:0,empty:!gid||gid==='0'});
          }
          out.sort((a,b)=>a.slot-b.slot);
          return JSON.stringify({buildings:out});
        })();"""
        runJs(js) { raw ->
            try {
                val decoded = JSONTokener(raw).nextValue() as? String ?: "{}"
                val buildings = org.json.JSONObject(decoded).optJSONArray("buildings") ?: JSONArray()
                val item = scanVillages.optJSONObject(scanIndex)
                val name = item?.optString("name") ?: "Selo"
                val resourceInfo = if (cachedResources(expectedDid) != null) "18 resursnih L10+ (dorf1 preskočen)" else scanResourceSummary
                scanResults.add("✓ $name | DID $expectedDid | $resourceInfo | centar \${buildings.length()} slotova")
                villageCache.edit().putString("center_$expectedDid", buildings.toString()).apply()
            } catch (_: Exception) {
                val item = scanVillages.optJSONObject(scanIndex)
                scanResults.add("✓ \${item?.optString("name") ?: "Selo"} | DID $expectedDid | resursi očitani; centar nije parsiran")
            }
            scanIndex++
            results.text = "Napredak: \${scanIndex}/\${scanVillages.length()} sela\n" + scanResults.takeLast(10).joinToString("\n")
            handler.postDelayed({ scanNextVillage() }, 900)
        }
    }

    private fun stopScan() {
        if (!scanRunning) { setStatus("Nema aktivnog učitavanja."); return }
        scanRunning = false
        results.text = "Učitavanje zaustavljeno na selu ${scanIndex + 1}/${scanVillages.length()}.\n" + scanResults.joinToString("\n")
        setStatus("Učitavanje zaustavljeno na zahtjev korisnika.")
    }

    private fun runJs(script: String, callback: (String) -> Unit) {
        if (!::web.isInitialized) return
        web.evaluateJavascript(script) { value -> callback(value ?: "") }
    }
    private fun jsString(value: String): String = "'" + value.replace("\\", "\\\\").replace("'", "\\'") + "'"
    private fun setStatus(message: String) { runOnUiThread { status.text = message } }

    override fun onBackPressed() {
        if (::web.isInitialized && web.canGoBack()) web.goBack() else super.onBackPressed()
    }
    override fun onDestroy() {
        scanRunning = false
        if (::web.isInitialized) web.destroy()
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }
}
