"use client";

import { useState } from "react";
import { Combine, FileImage, FileOutput, Minimize2, Scissors } from "lucide-react";

import { UploadDropzone } from "@/components/home/upload-dropzone";
import { ToolCard, type Tool } from "@/components/home/tool-card";

const tools: Tool[] = [
  { id: "img-to-pdf", icon: FileOutput, title: "Image to PDF", description: "Turn images into a polished, shareable PDF document.", targetFormat: "pdf" },
  { id: "pdf-to-img", icon: FileImage, title: "PDF to Image", description: "Export PDF pages into image files with clarity.", targetFormat: "png" },
  { id: "merge-pdf", icon: Combine, title: "Merge PDF", description: "Bring multiple PDFs together in one organized file.", targetFormat: "pdf" },
  { id: "split-pdf", icon: Scissors, title: "Split PDF", description: "Separate a PDF into focused pages or document sections.", targetFormat: "pdf" },
  { id: "compress-pdf", icon: Minimize2, title: "Compress PDF", description: "Reduce PDF file size while keeping documents useful.", targetFormat: "pdf" }
];

export function ToolsSection() {
  const [activeToolId, setActiveToolId] = useState<string>(tools[0].id);

  const activeTool = tools.find((t) => t.id === activeToolId) || tools[0];

  const handleToolSelect = (toolId: string) => {
    setActiveToolId(toolId);
    // Smooth scroll to the dropzone for better UX on mobile
    document.getElementById("upload-dropzone-section")?.scrollIntoView({ behavior: "smooth" });
  };

  return (
    <section id="tools" className="scroll-mt-20 border-y border-slate-800 bg-slate-900/30 px-4 py-20 sm:px-6 lg:px-8">
      <div className="mx-auto max-w-7xl">
        <div className="max-w-2xl">
          <p className="text-sm font-semibold uppercase tracking-[0.18em] text-sky-300">Conversion tools</p>
          <h2 className="mt-3 text-3xl font-semibold tracking-tight text-white sm:text-4xl">Everything essential, in one calm workspace.</h2>
          <p className="mt-4 text-base leading-7 text-slate-400">Pick a tool to get started. The interface is designed to keep routine document work simple.</p>
        </div>
        <div className="mt-10 grid gap-5 lg:grid-cols-[0.9fr_2fr]">
          <div id="upload-dropzone-section" className="scroll-mt-24 h-full">
            <UploadDropzone 
              supportedTypes="PDF, JPG, PNG, and more" 
              activeTool={activeTool} 
            />
          </div>
          <div className="grid gap-5 sm:grid-cols-2 xl:grid-cols-3">
            {tools.map((tool) => (
              <ToolCard 
                key={tool.id} 
                tool={tool} 
                isActive={tool.id === activeToolId}
                onSelect={handleToolSelect}
              />
            ))}
          </div>
        </div>
      </div>
    </section>
  );
}
