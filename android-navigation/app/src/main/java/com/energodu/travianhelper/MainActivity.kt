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

/**
 * Android navigation proof-of-concept based on Travian Helper v139.25 navigation.
 * This is not the full desktop helper port.
 */
class MainActivity : Activity() {
    private lateinit var web: WebView
    private lateinit var address: EditText
    private lateinit var villageDid: EditText
    private lateinit var status: TextView
    private val handler = Handler(Looper.getMainLooper())
    private var navigationAttempts = 0
    private var consecutiveMatches = 0
    private var expectedDid = ""
    private var requestedView = ""

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(10, 8, 10, 8)
        }
        address = EditText(this).apply {
            hint = "Travian server ili login link (https://...)"
            setSingleLine(true)
            setText("https://")
        }
        root.addView(address, LinearLayout.LayoutParams(-1, ViewGroup.LayoutParams.WRAP_CONTENT))
        root.addView(Button(this).apply {
            text = "Otvori Travian / prijava"
            setOnClickListener { openServer() }
        })
        val controls = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        controls.addView(makeButton("Provjeri selo") { readVillage() }, LinearLayout.LayoutParams(0, -2, 1f))
        controls.addView(makeButton("Resursi") { navigateView("resource") }, LinearLayout.LayoutParams(0, -2, 1f))
        controls.addView(makeButton("Centar") { navigateView("center") }, LinearLayout.LayoutParams(0, -2, 1f))
        root.addView(controls)
        val villageRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        villageDid = EditText(this).apply { hint = "ID sela (DID)"; setSingleLine(true) }
        villageRow.addView(villageDid, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        villageRow.addView(makeButton("Promijeni selo") {
            val did = villageDid.text.toString().trim()
            if (did.isNotEmpty()) switchToVillage(did, "center")
            else Toast.makeText(this, "Unesi DID sela", Toast.LENGTH_SHORT).show()
        })
        root.addView(villageRow)
        status = TextView(this).apply {
            text = "Spremno. Prijavi se u Travian kroz prozor ispod."
            textSize = 13f
            setPadding(4, 8, 4, 8)
        }
        root.addView(status)
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

    private fun makeButton(label: String, action: () -> Unit) = Button(this).apply {
        text = label
        setOnClickListener { action() }
    }

    private fun openServer() {
        var url = address.text.toString().trim()
        if (url.isBlank()) return
        if (!url.startsWith("https://") && !url.startsWith("http://")) url = "https://$url"
        if (!url.endsWith("login.php") && !url.contains("/dorf")) url = url.trimEnd('/') + "/login.php"
        web.loadUrl(url)
        setStatus("Otvaram Travian. Prijavu obavi u stranici igre.")
    }

    private fun readVillage() {
        runJs("""(function(){
          const sels=['.villageList .listEntry.village.active[data-did]','#sidebarBoxVillagelist .listEntry.village.active[data-did]','.listEntry.village.active[data-did]'];
          let e=null; for(const s of sels){e=document.querySelector(s); if(e) break;}
          let did=e?.getAttribute('data-did')||'';
          if(!did){const i=document.querySelector("input.villageInput[data-did],input[name='villageName'][data-did]"); did=i?.getAttribute('data-did')||'';}
          const name=e?.querySelector('span.name,.name')?.textContent?.trim()||'';
          return JSON.stringify({did:did,name:name,url:location.href});
        })();""") { result -> setStatus("Podaci sela: ${result}") }
    }

    private fun navigateView(view: String) {
        runJs("(function(){return location.href;})()") { currentUrl ->
            val targetPage = if (view == "resource") "dorf1.php" else "dorf2.php"
            if (currentUrl.contains(targetPage)) {
                setStatus("Već je otvoren traženi prikaz ($view).")
                return@runJs
            }
            val selector = if (view == "resource")
                "a.village.resourceView,a.resourceView,a[href*='dorf1.php']"
            else "a.village.buildingView,a.buildingView,a[href*='dorf2.php']"
            runJs("(function(){const e=document.querySelector(${jsString(selector)}); if(!e)return 'NO_BUTTON'; e.click(); return 'CLICKED';})()") { clickResult ->
                if (clickResult.contains("NO_BUTTON")) {
                    runJs("(function(){const u=new URL(location.href);u.pathname=u.pathname.replace(/[^/]+$/,${jsString(targetPage)});location.href=u.toString();return 'FALLBACK';})()") {
                        setStatus("Dugme nije pronađeno; pokušavam rezervni URL. Potrebna je naknadna provjera sela.")
                    }
                } else {
                    setStatus("Kliknuto Travian dugme za prikaz: $view. Provjeravam učitavanje…")
                    handler.postDelayed({ readVillage() }, 1600)
                }
            }
        }
    }

    private fun switchToVillage(did: String, view: String) {
        expectedDid = did
        requestedView = view
        navigationAttempts = 0
        consecutiveMatches = 0
        trySwitchAttempt()
    }

    private fun trySwitchAttempt() {
        if (expectedDid.isBlank()) { setStatus("STOP: DID sela nije zadat."); return }
        if (navigationAttempts >= 15) {
            setStatus("STOP: DID $expectedDid nije potvrđen poslije 15 pokušaja. Akcija nije izvršena.")
            return
        }
        navigationAttempts++
        val js = """(function(){
          const did=${jsString(expectedDid)};
          const e=document.querySelector('.villageList .listEntry.village[data-did="'+did+'"],#sidebarBoxVillagelist .listEntry.village[data-did="'+did+'"],.listEntry.village[data-did="'+did+'"]');
          if(!e) return 'NOT_FOUND';
          const t=e.querySelector('a,span.name,.name')||e; t.scrollIntoView({block:'center'}); t.click(); return 'CLICKED';
        })();"""
        runJs(js) { result ->
            if (result.contains("NOT_FOUND")) setStatus("Pokušaj $navigationAttempts/15: selo nije pronađeno u bočnoj listi.")
            else setStatus("Pokušaj $navigationAttempts/15: klik na selo; provjeravam DID…")
            handler.postDelayed({ verifyDidTwice() }, 400)
        }
    }

    private fun verifyDidTwice() {
        val js = """(function(){const e=document.querySelector('.villageList .listEntry.village.active[data-did],#sidebarBoxVillagelist .listEntry.village.active[data-did],.listEntry.village.active[data-did]'); if(e)return e.getAttribute('data-did')||''; const i=document.querySelector('input.villageInput[data-did],input[name="villageName"][data-did]');return i?.getAttribute('data-did')||'';})()"""
        runJs(js) { did ->
            if (did.trim('"') == expectedDid) consecutiveMatches++ else consecutiveMatches = 0
            if (consecutiveMatches >= 2) {
                setStatus("Potvrđen DID $expectedDid. Moguće je nastaviti sa prikazom $requestedView.")
                navigateView(requestedView)
            } else if (navigationAttempts < 15) {
                handler.postDelayed({ trySwitchAttempt() }, 350)
            } else {
                setStatus("STOP: selo nije pouzdano potvrđeno. Nema izvršavanja zadatka.")
            }
        }
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
        if (::web.isInitialized) web.destroy()
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }
}
