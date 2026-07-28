import { ChevronDown } from "lucide-react";

import { cn } from "@/lib/utils";

export type AccordionItem = {
  question: string;
  answer: string;
};

type AccordionProperties = {
  items: AccordionItem[];
  className?: string;
};

export function Accordion({ items, className }: AccordionProperties) {
  return (
    <div className={cn("divide-y divide-slate-800 rounded-2xl border border-slate-800 bg-slate-900/50 px-6", className)}>
      {items.map((item) => (
        <details key={item.question} className="group py-5">
          <summary className="flex cursor-pointer list-none items-center justify-between gap-6 text-left text-base font-semibold text-slate-100 marker:content-none">
            {item.question}
            <ChevronDown aria-hidden="true" className="h-5 w-5 shrink-0 text-sky-300 transition-transform group-open:rotate-180" />
          </summary>
          <p className="max-w-2xl pt-3 pr-8 text-sm leading-6 text-slate-400">{item.answer}</p>
        </details>
      ))}
    </div>
  );
}
