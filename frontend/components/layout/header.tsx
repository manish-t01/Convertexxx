import { Github, MoonStar } from "lucide-react";

import { Button } from "@/components/ui/button";

const navigationItems = [
  { href: "#home", label: "Home" },
  { href: "#tools", label: "Tools" },
  { href: "#about", label: "About" }
];

export function Header() {
  return (
    <header className="sticky top-0 z-50 border-b border-slate-800/80 bg-slate-950/85 backdrop-blur-lg">
      <div className="mx-auto flex min-h-16 max-w-7xl flex-wrap items-center justify-between gap-x-4 gap-y-2 px-4 py-3 sm:px-6 lg:px-8">
        <a href="#home" className="text-lg font-bold tracking-tight text-white" aria-label="Convertexxx home">
          Convertexxx
        </a>
        <nav aria-label="Main navigation" className="order-3 w-full sm:order-none sm:w-auto">
          <ul className="flex items-center justify-center gap-1 sm:gap-2">
            {navigationItems.map((item) => (
              <li key={item.href}>
                <a href={item.href} className="rounded-lg px-3 py-2 text-sm font-medium text-slate-400 transition-colors hover:bg-slate-900 hover:text-white">
                  {item.label}
                </a>
              </li>
            ))}
          </ul>
        </nav>
        <div className="flex items-center gap-1">
          <Button type="button" variant="ghost" size="icon" aria-label="Theme selector coming soon" title="Theme selector coming soon">
            <MoonStar aria-hidden="true" className="h-4 w-4" />
          </Button>
          <Button type="button" variant="ghost" size="icon" aria-label="GitHub link placeholder" title="GitHub link placeholder">
            <Github aria-hidden="true" className="h-4 w-4" />
          </Button>
        </div>
      </div>
    </header>
  );
}
