import { Combine, FileImage, FileOutput, Minimize2, Scissors } from "lucide-react";

import { UploadDropzone } from "@/components/home/upload-dropzone";
import { ToolCard, type Tool } from "@/components/home/tool-card";

const tools: Tool[] = [
  { icon: FileOutput, title: "Image to PDF", description: "Turn images into a polished, shareable PDF document." },
  { icon: FileImage, title: "PDF to Image", description: "Export PDF pages into image files with clarity." },
  { icon: Combine, title: "Merge PDF", description: "Bring multiple PDFs together in one organized file." },
  { icon: Scissors, title: "Split PDF", description: "Separate a PDF into focused pages or document sections." },
  { icon: Minimize2, title: "Compress PDF", description: "Reduce PDF file size while keeping documents useful." }
];

export function ToolsSection() {
  return (
    <section id="tools" className="scroll-mt-20 border-y border-slate-800 bg-slate-900/30 px-4 py-20 sm:px-6 lg:px-8">
      <div className="mx-auto max-w-7xl">
        <div className="max-w-2xl">
          <p className="text-sm font-semibold uppercase tracking-[0.18em] text-sky-300">Conversion tools</p>
          <h2 className="mt-3 text-3xl font-semibold tracking-tight text-white sm:text-4xl">Everything essential, in one calm workspace.</h2>
          <p className="mt-4 text-base leading-7 text-slate-400">Pick a tool to get started. The interface is designed to keep routine document work simple.</p>
        </div>
        <div className="mt-10 grid gap-5 lg:grid-cols-[0.9fr_2fr]">
          <UploadDropzone supportedTypes="PDF, JPG, PNG, and more" />
          <div className="grid gap-5 sm:grid-cols-2 xl:grid-cols-3">
            {tools.map((tool) => (
              <ToolCard key={tool.title} tool={tool} />
            ))}
          </div>
        </div>
      </div>
    </section>
  );
}
