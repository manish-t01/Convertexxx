import { Gauge, LockKeyhole, Palette, ShieldCheck } from "lucide-react";

import type { LucideIcon } from "lucide-react";

type Feature = {
  icon: LucideIcon;
  title: string;
  description: string;
};

const features: Feature[] = [
  { icon: ShieldCheck, title: "Privacy first", description: "A focused product philosophy that puts control of your documents first." },
  { icon: Gauge, title: "Fast conversion", description: "A streamlined interface designed to make common tasks feel immediate." },
  { icon: LockKeyhole, title: "Secure processing", description: "Clear workflows that keep the document experience straightforward and trustworthy." },
  { icon: Palette, title: "Modern UI", description: "Thoughtful details, readable layouts, and a responsive experience on every screen." }
];

export function FeaturesSection() {
  return (
    <section id="about" className="scroll-mt-20 px-4 py-20 sm:px-6 lg:px-8">
      <div className="mx-auto max-w-7xl">
        <div className="max-w-2xl">
          <p className="text-sm font-semibold uppercase tracking-[0.18em] text-sky-300">Built with intention</p>
          <h2 className="mt-3 text-3xl font-semibold tracking-tight text-white sm:text-4xl">A better baseline for document tools.</h2>
        </div>
        <div className="mt-10 grid gap-5 sm:grid-cols-2 lg:grid-cols-4">
          {features.map((feature) => {
            const Icon = feature.icon;

            return (
              <article key={feature.title} className="rounded-2xl border border-slate-800 p-6">
                <Icon aria-hidden="true" className="h-5 w-5 text-sky-300" />
                <h3 className="mt-5 font-semibold text-slate-100">{feature.title}</h3>
                <p className="mt-2 text-sm leading-6 text-slate-400">{feature.description}</p>
              </article>
            );
          })}
        </div>
      </div>
    </section>
  );
}
