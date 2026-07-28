import { CloudUpload } from "lucide-react";

import { Button } from "@/components/ui/button";

type UploadDropzoneProperties = {
  supportedTypes: string;
};

export function UploadDropzone({ supportedTypes }: UploadDropzoneProperties) {
  return (
    <div className="rounded-2xl border border-dashed border-slate-700 bg-slate-900/60 p-6 text-center sm:p-8">
      <div className="mx-auto flex h-12 w-12 items-center justify-center rounded-xl bg-sky-400/10 text-sky-300">
        <CloudUpload aria-hidden="true" className="h-6 w-6" />
      </div>
      <h3 className="mt-4 text-lg font-semibold text-white">Drop your files here</h3>
      <p className="mt-2 text-sm text-slate-400">Drag and drop your documents, or choose files from your device.</p>
      <Button type="button" variant="outline" size="sm" className="mt-5">
        Browse files
      </Button>
      <p className="mt-4 text-xs text-slate-500">Supported formats: {supportedTypes}</p>
    </div>
  );
}
