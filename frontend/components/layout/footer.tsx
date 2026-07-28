import { Github } from "lucide-react";

const quickLinks = [
  { href: "#tools", label: "Tools" },
  { href: "#about", label: "About" },
  { href: "#faq", label: "FAQ" }
];

export function Footer() {
  return (
    <footer className="border-t border-slate-800 bg-slate-950">
      <div className="mx-auto flex max-w-7xl flex-col gap-6 px-4 py-10 sm:px-6 md:flex-row md:items-center md:justify-between lg:px-8">
        <div>
          <p className="font-semibold text-slate-100">Convertexxx</p>
          <p className="mt-1 text-sm text-slate-500">© 2026 Convertexxx. Built for simpler document work.</p>
        </div>
        <div className="flex flex-wrap items-center gap-x-6 gap-y-3 text-sm">
          {quickLinks.map((link) => (
            <a key={link.href} href={link.href} className="text-slate-400 transition-colors hover:text-sky-300">
              {link.label}
            </a>
          ))}
          <a href="#github" className="inline-flex items-center gap-2 text-slate-400 transition-colors hover:text-sky-300" aria-label="GitHub placeholder">
            <Github aria-hidden="true" className="h-4 w-4" />
            GitHub
          </a>
          <span className="rounded-full border border-slate-800 px-2.5 py-1 text-xs text-slate-500">v0.1.0</span>
        </div>
      </div>
    </footer>
  );
}
