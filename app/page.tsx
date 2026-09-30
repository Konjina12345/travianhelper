export default function Home() {
  return (
    <main className="min-h-screen bg-slate-950 text-white">
      
      {/* HEADER */}
      <header className="border-b border-slate-800">
        <div className="mx-auto flex max-w-6xl items-center justify-between px-6 py-5">
          <div>
            <h1 className="text-2xl font-bold">
              TRAVIAN<span className="text-red-500">HELPER</span>
            </h1>
            <p className="text-xs text-slate-400">
              Automatizacija za Travian
            </p>
          </div>

          <button className="rounded-lg border border-slate-700 px-5 py-2 text-sm font-medium hover:bg-slate-800">
            Prijava
          </button>
        </div>
      </header>

      {/* HERO */}
      <section className="mx-auto max-w-6xl px-6 py-24 text-center">
        <div className="mx-auto max-w-3xl">
          <div className="mb-5 inline-block rounded-full border border-red-500/30 bg-red-500/10 px-4 py-2 text-sm text-red-400">
            Travian Helper
          </div>

          <h2 className="text-5xl font-bold tracking-tight md:text-6xl">
            Igraj pametnije.
            <br />
            <span className="text-red-500">Automatizuj ponavljajuće zadatke.</span>
          </h2>

          <p className="mx-auto mt-6 max-w-2xl text-lg leading-8 text-slate-400">
            TravianHelper je alat napravljen za automatizaciju svakodnevnih
            Travian zadataka. Jednostavno pokretanje, pregledna kontrola i
            aktivna licenca.
          </p>

          <div className="mt-10 flex flex-col justify-center gap-4 sm:flex-row">
            <button className="rounded-lg bg-red-600 px-7 py-3 font-semibold hover:bg-red-500">
              Pogledaj licence
            </button>

            <button className="rounded-lg border border-slate-700 px-7 py-3 font-semibold hover:bg-slate-800">
              Kako radi?
            </button>
          </div>
        </div>
      </section>

      {/* FEATURES */}
      <section className="border-y border-slate-800 bg-slate-900/50">
        <div className="mx-auto grid max-w-6xl gap-6 px-6 py-16 md:grid-cols-3">
          
          <div className="rounded-xl border border-slate-800 bg-slate-950 p-6">
            <div className="mb-4 text-3xl">⚡</div>
            <h3 className="text-xl font-semibold">Jednostavno</h3>
            <p className="mt-2 text-slate-400">
              Pokreni helper i prati njegov rad kroz jednostavan sistem.
            </p>
          </div>

          <div className="rounded-xl border border-slate-800 bg-slate-950 p-6">
            <div className="mb-4 text-3xl">🔑</div>
            <h3 className="text-xl font-semibold">Licenca</h3>
            <p className="mt-2 text-slate-400">
              Svaki korisnik ima svoju licencu i definisan period korištenja.
            </p>
          </div>

          <div className="rounded-xl border border-slate-800 bg-slate-950 p-6">
            <div className="mb-4 text-3xl">🛡️</div>
            <h3 className="text-xl font-semibold">Kontrola</h3>
            <p className="mt-2 text-slate-400">
              Korisnički panel omogućava pregled licence i njenog statusa.
            </p>
          </div>

        </div>
      </section>

      {/* PRICING */}
      <section className="mx-auto max-w-6xl px-6 py-20">
        <div className="text-center">
          <h2 className="text-3xl font-bold">Licence</h2>
          <p className="mt-3 text-slate-400">
            Izaberi period korištenja TravianHelper-a.
          </p>
        </div>

        <div className="mt-12 grid gap-6 md:grid-cols-4">

          <div className="rounded-xl border border-slate-800 bg-slate-900 p-6">
            <h3 className="font-semibold">7 dana</h3>
            <div className="mt-4 text-3xl font-bold">3 €</div>
            <button className="mt-6 w-full rounded-lg bg-slate-800 py-3 font-medium hover:bg-slate-700">
              Izaberi
            </button>
          </div>

          <div className="rounded-xl border border-red-500/50 bg-slate-900 p-6">
            <div className="mb-2 text-xs font-bold uppercase text-red-400">
              Popularno
            </div>
            <h3 className="font-semibold">30 dana</h3>
            <div className="mt-4 text-3xl font-bold">8 €</div>
            <button className="mt-6 w-full rounded-lg bg-red-600 py-3 font-semibold hover:bg-red-500">
              Izaberi
            </button>
          </div>

          <div className="rounded-xl border border-slate-800 bg-slate-900 p-6">
            <h3 className="font-semibold">90 dana</h3>
            <div className="mt-4 text-3xl font-bold">20 €</div>
            <button className="mt-6 w-full rounded-lg bg-slate-800 py-3 font-medium hover:bg-slate-700">
              Izaberi
            </button>
          </div>

          <div className="rounded-xl border border-slate-800 bg-slate-900 p-6">
            <h3 className="font-semibold">180 dana</h3>
            <div className="mt-4 text-3xl font-bold">35 €</div>
            <button className="mt-6 w-full rounded-lg bg-slate-800 py-3 font-medium hover:bg-slate-700">
              Izaberi
            </button>
          </div>

        </div>
      </section>

      {/* FOOTER */}
      <footer className="border-t border-slate-800">
        <div className="mx-auto flex max-w-6xl flex-col justify-between gap-4 px-6 py-8 text-sm text-slate-500 md:flex-row">
          <div>© 2026 TravianHelper</div>
          <div>Travian Helper System</div>
        </div>
      </footer>

    </main>
  );
}