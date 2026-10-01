const features = [
  {
    number: "01",
    title: "Automatizacija",
    text: "Automatizuj ponavljajuće Travian zadatke i uštedi vrijeme.",
  },
  {
    number: "02",
    title: "Jednostavna kontrola",
    text: "Pokretanje i kontrola helpera kroz jednostavan sistem.",
  },
  {
    number: "03",
    title: "Licenciranje",
    text: "Svaka licenca ima svoj rok trajanja i kontrolu uređaja.",
  },
  {
    number: "04",
    title: "Korisnički panel",
    text: "Na jednom mjestu vidi status licence, datum isteka i podatke.",
  },
];

const plans = [
  {
    name: "7 DANA",
    price: "3 €",
    description: "Za kratko testiranje",
  },
  {
    name: "30 DANA",
    price: "8 €",
    description: "Za redovno korištenje",
    popular: true,
  },
  {
    name: "90 DANA",
    price: "20 €",
    description: "Dugoročno korištenje",
  },
  {
    name: "180 DANA",
    price: "35 €",
    description: "Za dugoročne igrače",
  },
];

export default function Home() {
  return (
    <main className="min-h-screen bg-[#08090c] text-white">

      {/* NAVBAR */}
      <header className="sticky top-0 z-50 border-b border-white/10 bg-[#08090c]/90 backdrop-blur">
        <div className="mx-auto flex max-w-7xl items-center justify-between px-6 py-5">

          <a href="#" className="text-xl font-black tracking-tight">
            TRAVIAN<span className="text-red-500">HELPER</span>
          </a>

          <nav className="hidden items-center gap-8 text-sm text-gray-400 md:flex">
            <a href="#funkcije" className="transition hover:text-white">
              Funkcije
            </a>
            <a href="#licence" className="transition hover:text-white">
              Licence
            </a>
            <a href="#kako-radi" className="transition hover:text-white">
              Kako radi?
            </a>
          </nav>

          <a
            href="#prijava"
            className="rounded-lg border border-white/15 px-5 py-2.5 text-sm font-semibold transition hover:border-red-500 hover:bg-red-500/10"
          >
            Prijava
          </a>
        </div>
      </header>

      {/* HERO */}
      <section className="relative overflow-hidden">
        <div className="absolute left-1/2 top-0 h-[500px] w-[800px] -translate-x-1/2 rounded-full bg-red-600/10 blur-[140px]" />

        <div className="relative mx-auto max-w-7xl px-6 pb-24 pt-28 text-center md:pb-32 md:pt-36">

          <div className="mx-auto mb-7 inline-flex items-center gap-2 rounded-full border border-red-500/20 bg-red-500/5 px-4 py-2 text-sm text-red-400">
            <span className="h-2 w-2 rounded-full bg-red-500" />
            TRAVIAN HELPER SYSTEM
          </div>

          <h1 className="mx-auto max-w-5xl text-5xl font-black leading-tight tracking-tight md:text-7xl">
            Više vremena za igru.
            <br />
            <span className="text-red-500">
              Manje ponavljajućih zadataka.
            </span>
          </h1>

          <p className="mx-auto mt-7 max-w-2xl text-lg leading-8 text-gray-400">
            TravianHelper je alat za automatizaciju svakodnevnih zadataka
            u Travianu, sa sistemom licenci i korisničkim panelom.
          </p>

          <div className="mt-10 flex flex-col justify-center gap-4 sm:flex-row">
            <a
              href="#licence"
              className="rounded-lg bg-red-600 px-8 py-3.5 font-bold transition hover:bg-red-500"
            >
              Pogledaj licence
            </a>

            <a
              href="#kako-radi"
              className="rounded-lg border border-white/15 px-8 py-3.5 font-semibold transition hover:border-white/30 hover:bg-white/5"
            >
              Kako radi?
            </a>
          </div>

          <div className="mx-auto mt-14 grid max-w-3xl grid-cols-2 gap-4 md:grid-cols-4">
            <div className="rounded-xl border border-white/10 bg-white/[0.03] p-5">
              <div className="text-2xl font-black">24/7</div>
              <div className="mt-1 text-xs text-gray-500">Licenca</div>
            </div>

            <div className="rounded-xl border border-white/10 bg-white/[0.03] p-5">
              <div className="text-2xl font-black">4</div>
              <div className="mt-1 text-xs text-gray-500">Paketa</div>
            </div>

            <div className="rounded-xl border border-white/10 bg-white/[0.03] p-5">
              <div className="text-2xl font-black">1</div>
              <div className="mt-1 text-xs text-gray-500">Nalog</div>
            </div>

            <div className="rounded-xl border border-white/10 bg-white/[0.03] p-5">
              <div className="text-2xl font-black">∞</div>
              <div className="mt-1 text-xs text-gray-500">Kontrola</div>
            </div>
          </div>
        </div>
      </section>

      {/* FEATURES */}
      <section id="funkcije" className="border-y border-white/10 bg-[#0c0e12]">
        <div className="mx-auto max-w-7xl px-6 py-24">

          <div className="max-w-2xl">
            <div className="text-sm font-bold uppercase tracking-widest text-red-500">
              Funkcije
            </div>

            <h2 className="mt-4 text-4xl font-black tracking-tight md:text-5xl">
              Sve što ti treba.
              <br />
              Na jednom mjestu.
            </h2>

            <p className="mt-5 leading-7 text-gray-400">
              TravianHelper je napravljen sa fokusom na jednostavnost,
              kontrolu licence i pregledno korištenje.
            </p>
          </div>

          <div className="mt-14 grid gap-px overflow-hidden rounded-2xl border border-white/10 bg-white/10 md:grid-cols-2">

            {features.map((feature) => (
              <div
                key={feature.number}
                className="bg-[#0c0e12] p-8 transition hover:bg-white/[0.03]"
              >
                <div className="text-sm font-bold text-red-500">
                  {feature.number}
                </div>

                <h3 className="mt-5 text-2xl font-bold">
                  {feature.title}
                </h3>

                <p className="mt-3 max-w-md leading-7 text-gray-400">
                  {feature.text}
                </p>
              </div>
            ))}

          </div>
        </div>
      </section>

      {/* HOW IT WORKS */}
      <section id="kako-radi" className="mx-auto max-w-7xl px-6 py-24">

        <div className="text-center">
          <div className="text-sm font-bold uppercase tracking-widest text-red-500">
            Jednostavno
          </div>

          <h2 className="mt-4 text-4xl font-black">
            Kako radi?
          </h2>
        </div>

        <div className="mt-14 grid gap-6 md:grid-cols-3">

          <div className="rounded-2xl border border-white/10 bg-white/[0.02] p-8">
            <div className="text-4xl font-black text-red-500">01</div>
            <h3 className="mt-6 text-xl font-bold">
              Napravi nalog
            </h3>
            <p className="mt-3 leading-7 text-gray-400">
              Registruješ se na TravianHelper i dobijaš svoj korisnički panel.
            </p>
          </div>

          <div className="rounded-2xl border border-white/10 bg-white/[0.02] p-8">
            <div className="text-4xl font-black text-red-500">02</div>
            <h3 className="mt-6 text-xl font-bold">
              Aktiviraj licencu
            </h3>
            <p className="mt-3 leading-7 text-gray-400">
              Izabereš period korištenja i dobijaš licencu povezanu sa nalogom.
            </p>
          </div>

          <div className="rounded-2xl border border-white/10 bg-white/[0.02] p-8">
            <div className="text-4xl font-black text-red-500">03</div>
            <h3 className="mt-6 text-xl font-bold">
              Pokreni helper
            </h3>
            <p className="mt-3 leading-7 text-gray-400">
              TravianHelper provjerava licencu i omogućava korištenje alata.
            </p>
          </div>

        </div>
      </section>

      {/* PRICING */}
      <section id="licence" className="border-y border-white/10 bg-[#0c0e12]">
        <div className="mx-auto max-w-7xl px-6 py-24">

          <div className="text-center">
            <div className="text-sm font-bold uppercase tracking-widest text-red-500">
              Licence
            </div>

            <h2 className="mt-4 text-4xl font-black md:text-5xl">
              Izaberi svoj period
            </h2>

            <p className="mx-auto mt-5 max-w-xl text-gray-400">
              Cijene su trenutno prikazane kao primjer i kasnije ih možemo
              promijeniti kroz sistem.
            </p>
          </div>

          <div className="mt-14 grid gap-6 md:grid-cols-4">

            {plans.map((plan) => (
              <div
                key={plan.name}
                className={`relative rounded-2xl border p-7 ${
                  plan.popular
                    ? "border-red-500 bg-red-500/[0.05]"
                    : "border-white/10 bg-[#08090c]"
                }`}
              >

                {plan.popular && (
                  <div className="absolute -top-3 left-6 rounded-full bg-red-600 px-3 py-1 text-xs font-bold">
                    POPULARNO
                  </div>
                )}

                <div className="text-sm font-bold text-gray-400">
                  {plan.name}
                </div>

                <div className="mt-5 text-4xl font-black">
                  {plan.price}
                </div>

                <p className="mt-3 text-sm text-gray-500">
                  {plan.description}
                </p>

                <button
                  className={`mt-8 w-full rounded-lg py-3 font-bold transition ${
                    plan.popular
                      ? "bg-red-600 hover:bg-red-500"
                      : "bg-white/10 hover:bg-white/15"
                  }`}
                >
                  Izaberi
                </button>

              </div>
            ))}

          </div>
        </div>
      </section>

      {/* CTA */}
      <section className="mx-auto max-w-7xl px-6 py-24">
        <div className="relative overflow-hidden rounded-3xl border border-red-500/20 bg-red-500/[0.06] px-6 py-16 text-center md:px-16">

          <div className="absolute left-1/2 top-0 h-64 w-96 -translate-x-1/2 rounded-full bg-red-500/10 blur-[100px]" />

          <div className="relative">
            <h2 className="text-4xl font-black md:text-5xl">
              Spreman za TravianHelper?
            </h2>

            <p className="mx-auto mt-5 max-w-xl text-gray-400">
              Kreiraj nalog i izaberi licencu koja ti odgovara.
            </p>

            <a
              href="#licence"
              className="mt-8 inline-block rounded-lg bg-red-600 px-8 py-3.5 font-bold transition hover:bg-red-500"
            >
              Pogledaj licence
            </a>
          </div>

        </div>
      </section>

      {/* FOOTER */}
      <footer className="border-t border-white/10">
        <div className="mx-auto flex max-w-7xl flex-col gap-4 px-6 py-8 text-sm text-gray-500 md:flex-row md:items-center md:justify-between">

          <div>
            © 2026 TravianHelper
          </div>

          <div>
            Travian Helper System
          </div>

        </div>
      </footer>

    </main>
  );
}