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
    private val scanResults = mutableListOf<String>()
    private var scanResourceSummary = ""

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
        expectedDid = item.optString("did"); requestedView = "scan"; navigationAttempts = 0; consecutiveMatches = 0
        setStatus("Selo ${scanIndex + 1}/${scanVillages.length()}: ${item.optString("name")} (DID $expectedDid) — prebacujem…")
        scanSwitchAttempt()
    }

    private fun scanSwitchAttempt() {
        if (!scanRunning) return
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
                setStatus("DID $expectedDid potvrđen — učitavam resurse…")
                scanOpenResource()
            } else if (navigationAttempts < 15) handler.postDelayed({ scanSwitchAttempt() }, 450)
            else { scanResults.add("GREŠKA DID $expectedDid — potvrda nije uspjela"); scanIndex++; scanNextVillage() }
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

    private fun captureResourcePage() {
        if (!scanRunning) return
        val js = """(function(){
          const stock=document.querySelector('#stockBar')?.innerText?.replace(/\s+/g,' ')||'';
          const fields=[...document.querySelectorAll('#resourceFieldContainer .resourceField, .resourceField')];
          const levels=fields.map(e=>{const s=(e.getAttribute('title')||e.innerText||'');const m=s.match(/(?:level|level\s*|lvl|nivo|stufe)\s*[: ]?\s*(\d+)/i);return m?parseInt(m[1]):null;});
          return JSON.stringify({stock:stock.slice(0,220),fieldCount:fields.length,levels:levels});
        })();"""
        runJs(js) { raw ->
            scanResourceSummary = raw.take(240)
            setStatus("DID $expectedDid — resursi učitani; prelazim u centar…")
            runJs("(function(){return location.href;})()") { url ->
                if (url.contains("dorf2.php")) captureCenterPage()
                else clickView("center") { ok ->
                    if (!ok) { scanResults.add("DID $expectedDid — resursi učitani, centar nije otvoren"); scanIndex++; scanNextVillage() }
                    else captureCenterPage()
                }
            }
        }
    }

    private fun captureCenterPage() {
        if (!scanRunning) return
        val js = """(function(){
          const title=document.querySelector('#villageNameField,.titleInHeader,.contentTitle')?.innerText?.trim()||document.title;
          const buildings=[...document.querySelectorAll('#village_map .buildingSlot, #village_map .building, .buildingList .building')];
          const count=buildings.length;
          return JSON.stringify({title:title,buildingCount:count,url:location.pathname});
        })();"""
        runJs(js) { center ->
            val item = scanVillages.optJSONObject(scanIndex)
            val name = item?.optString("name") ?: "Selo"
            scanResults.add("✓ $name | DID $expectedDid | resursi + centar učitani")
            scanIndex++
            results.text = "Napredak: ${scanIndex}/${scanVillages.length()} sela\n" + scanResults.takeLast(8).joinToString("\n")
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
