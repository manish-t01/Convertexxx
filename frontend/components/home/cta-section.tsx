import { ArrowRight } from "lucide-react";

import { Button } from "@/components/ui/button";

export function CtaSection() {
  return (
    <section id="start" className="px-4 py-20 sm:px-6 lg:px-8">
      <div className="mx-auto max-w-7xl rounded-3xl border border-sky-400/20 bg-gradient-to-br from-sky-400/15 via-slate-900 to-slate-900 px-6 py-14 text-center sm:px-12">
        <p className="text-sm font-semibold uppercase tracking-[0.18em] text-sky-300">Ready when you are</p>
        <h2 className="mx-auto mt-3 max-w-2xl text-3xl font-semibold tracking-tight text-white sm:text-4xl">Start with the tools that make document work feel lighter.</h2>
        <p className="mx-auto mt-4 max-w-xl leading-7 text-slate-300">Explore the focused interface and choose the workflow you need.</p>
        <Button asChild size="lg" className="mt-8">
          <a href="#tools">
            Start converting
            <ArrowRight aria-hidden="true" className="h-4 w-4" />
          </a>
        </Button>
      </div>
    </section>
  );
}
