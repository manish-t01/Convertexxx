import { ArrowRight, Sparkles } from "lucide-react";

import { Button } from "@/components/ui/button";

export function HeroSection() {
  return (
    <section id="home" className="relative overflow-hidden px-4 pb-20 pt-20 sm:px-6 sm:pb-28 sm:pt-28 lg:px-8">
      <div aria-hidden="true" className="absolute inset-x-0 top-0 -z-0 h-[32rem] bg-[radial-gradient(ellipse_at_top,_var(--tw-gradient-stops))] from-sky-500/15 via-slate-950 to-slate-950" />
      <div className="relative z-10 mx-auto max-w-4xl text-center">
        <div className="inline-flex items-center gap-2 rounded-full border border-sky-400/20 bg-sky-400/10 px-3 py-1.5 text-sm font-medium text-sky-200">
          <Sparkles aria-hidden="true" className="h-4 w-4" />
          Simple, private document tools
        </div>
        <h1 className="mt-6 text-balance text-4xl font-semibold tracking-tight text-white sm:text-6xl lg:text-7xl">
          Convert documents without the clutter.
        </h1>
        <p className="mx-auto mt-6 max-w-2xl text-pretty text-lg leading-8 text-slate-300 sm:text-xl">
          A fast, focused workspace for everyday PDF and image tasks—built around privacy, clarity, and a better conversion experience.
        </p>
        <div className="mt-9 flex flex-col justify-center gap-3 sm:flex-row">
          <Button asChild size="lg">
            <a href="#tools">
              Explore tools
              <ArrowRight aria-hidden="true" className="h-4 w-4" />
            </a>
          </Button>
          <Button asChild variant="outline" size="lg">
            <a href="#about">Why Convertexxx</a>
          </Button>
        </div>
      </div>
    </section>
  );
}
