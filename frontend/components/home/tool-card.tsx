import type { LucideIcon } from "lucide-react";
import { ArrowUpRight, CheckCircle2 } from "lucide-react";

import { Button } from "@/components/ui/button";

export type Tool = {
  id: string;
  icon: LucideIcon;
  title: string;
  description: string;
  targetFormat: string;
};

type ToolCardProperties = {
  tool: Tool;
  isActive?: boolean;
  onSelect?: (toolId: string) => void;
};

export function ToolCard({ tool, isActive, onSelect }: ToolCardProperties) {
  const Icon = tool.icon;

  return (
    <article
      onClick={() => onSelect?.(tool.id)}
      className={`group relative flex h-full cursor-pointer flex-col rounded-2xl border bg-slate-900/50 p-6 transition-colors hover:bg-slate-900 ${
        isActive ? "border-sky-400" : "border-slate-800 hover:border-sky-400/40"
      }`}
    >
      {isActive && (
        <div className="absolute right-4 top-4 text-sky-400">
          <CheckCircle2 className="h-5 w-5" />
        </div>
      )}
      <div className="flex h-11 w-11 items-center justify-center rounded-xl bg-sky-400/10 text-sky-300">
        <Icon aria-hidden="true" className="h-5 w-5" />
      </div>
      <h3 className="mt-5 text-lg font-semibold text-white">{tool.title}</h3>
      <p className="mt-2 flex-1 text-sm leading-6 text-slate-400">{tool.description}</p>
      <Button
        type="button"
        variant="ghost"
        size="sm"
        className={`mt-6 w-fit px-0 hover:bg-transparent ${
          isActive ? "text-sky-400" : "text-sky-300 hover:text-sky-200"
        }`}
      >
        {isActive ? "Selected tool" : "Choose tool"}
        {!isActive && <ArrowUpRight aria-hidden="true" className="ml-1 h-4 w-4" />}
      </Button>
    </article>
  );
}
