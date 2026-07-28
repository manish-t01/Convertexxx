import type { LucideIcon } from "lucide-react";
import { ArrowUpRight } from "lucide-react";

import { Button } from "@/components/ui/button";

export type Tool = {
  icon: LucideIcon;
  title: string;
  description: string;
};

type ToolCardProperties = {
  tool: Tool;
};

export function ToolCard({ tool }: ToolCardProperties) {
  const Icon = tool.icon;

  return (
    <article className="group flex h-full flex-col rounded-2xl border border-slate-800 bg-slate-900/50 p-6 transition-colors hover:border-sky-400/40 hover:bg-slate-900">
      <div className="flex h-11 w-11 items-center justify-center rounded-xl bg-sky-400/10 text-sky-300">
        <Icon aria-hidden="true" className="h-5 w-5" />
      </div>
      <h3 className="mt-5 text-lg font-semibold text-white">{tool.title}</h3>
      <p className="mt-2 flex-1 text-sm leading-6 text-slate-400">{tool.description}</p>
      <Button type="button" variant="ghost" size="sm" className="mt-6 w-fit px-0 text-sky-300 hover:bg-transparent hover:text-sky-200">
        Choose tool
        <ArrowUpRight aria-hidden="true" className="h-4 w-4" />
      </Button>
    </article>
  );
}
