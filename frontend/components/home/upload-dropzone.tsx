"use client";

import { useRef, useState } from "react";
import { CloudUpload, File, CheckCircle, XCircle, Loader2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import { uploadFile } from "@/services/api";

type UploadDropzoneProperties = {
  supportedTypes: string;
};

export function UploadDropzone({ supportedTypes }: UploadDropzoneProperties) {
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [isDragging, setIsDragging] = useState(false);
  const [uploading, setUploading] = useState(false);
  const [progress, setProgress] = useState(0);
  const [status, setStatus] = useState<"idle" | "success" | "error">("idle");
  const [message, setMessage] = useState<string>("");

  const fileInputRef = useRef<HTMLInputElement>(null);

  const handleDragOver = (e: React.DragEvent<HTMLDivElement>) => {
    e.preventDefault();
    setIsDragging(true);
  };

  const handleDragLeave = () => {
    setIsDragging(false);
  };

  const handleDrop = (e: React.DragEvent<HTMLDivElement>) => {
    e.preventDefault();
    setIsDragging(false);
    
    if (e.dataTransfer.files && e.dataTransfer.files.length > 0) {
      processFileSelection(e.dataTransfer.files[0]);
    }
  };

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files.length > 0) {
      processFileSelection(e.target.files[0]);
    }
  };

  const processFileSelection = (file: File) => {
    setSelectedFile(file);
    setStatus("idle");
    setMessage("");
    setProgress(0);
  };

  const handleUpload = async () => {
    if (!selectedFile) return;

    setUploading(true);
    setProgress(0);
    setStatus("idle");

    try {
      // Defaulting to "pdf" target format for now as per initial instructions
      const response = await uploadFile(selectedFile, "pdf", (percentCompleted) => {
        setProgress(percentCompleted);
      });

      if (response.success) {
        setStatus("success");
        setMessage("Upload successful");
      } else {
        setStatus("error");
        setMessage(response.message || "Upload failed");
      }
    } catch (error: any) {
      setStatus("error");
      setMessage(error.response?.data?.message || "Upload failed");
    } finally {
      setUploading(false);
    }
  };

  const formatFileSize = (bytes: number) => {
    if (bytes === 0) return "0 Bytes";
    const k = 1024;
    const sizes = ["Bytes", "KB", "MB", "GB"];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + " " + sizes[i];
  };

  return (
    <div
      className={`relative flex flex-col items-center justify-center rounded-2xl border border-dashed bg-slate-900/60 p-6 text-center sm:p-8 transition-colors ${
        isDragging ? "border-sky-400 bg-slate-800/80" : "border-slate-700"
      }`}
      onDragOver={handleDragOver}
      onDragLeave={handleDragLeave}
      onDrop={handleDrop}
    >
      {!selectedFile ? (
        <>
          <div className="mx-auto flex h-12 w-12 items-center justify-center rounded-xl bg-sky-400/10 text-sky-300">
            <CloudUpload aria-hidden="true" className="h-6 w-6" />
          </div>
          <h3 className="mt-4 text-lg font-semibold text-white">Drop your files here</h3>
          <p className="mt-2 text-sm text-slate-400">Drag and drop your documents, or choose files from your device.</p>
          
          <input
            type="file"
            ref={fileInputRef}
            className="hidden"
            onChange={handleFileChange}
          />
          
          <Button 
            type="button" 
            variant="outline" 
            size="sm" 
            className="mt-5"
            onClick={() => fileInputRef.current?.click()}
          >
            Browse files
          </Button>
          <p className="mt-4 text-xs text-slate-500">Supported formats: {supportedTypes}</p>
        </>
      ) : (
        <div className="flex w-full flex-col items-center">
          <div className="mx-auto flex h-12 w-12 items-center justify-center rounded-xl bg-sky-400/10 text-sky-300">
            <File aria-hidden="true" className="h-6 w-6" />
          </div>
          <h3 className="mt-4 max-w-full truncate text-lg font-semibold text-white px-4">
            {selectedFile.name}
          </h3>
          <p className="mt-1 text-sm text-slate-400">{formatFileSize(selectedFile.size)}</p>

          {/* Progress Bar */}
          {uploading && (
            <div className="mt-6 w-full max-w-xs space-y-2">
              <div className="flex justify-between text-xs text-slate-400">
                <span>Uploading...</span>
                <span>{progress}%</span>
              </div>
              <div className="h-1.5 w-full overflow-hidden rounded-full bg-slate-800">
                <div 
                  className="h-full rounded-full bg-sky-400 transition-all duration-300 ease-in-out"
                  style={{ width: `${progress}%` }}
                />
              </div>
            </div>
          )}

          {/* Status Message */}
          {status !== "idle" && (
            <div className={`mt-4 flex items-center gap-2 text-sm font-medium ${
              status === "success" ? "text-emerald-400" : "text-rose-400"
            }`}>
              {status === "success" ? <CheckCircle className="h-4 w-4" /> : <XCircle className="h-4 w-4" />}
              {message}
            </div>
          )}

          <div className="mt-6 flex gap-3">
            <Button 
              type="button" 
              variant="outline" 
              size="sm"
              disabled={uploading}
              onClick={() => {
                setSelectedFile(null);
                setStatus("idle");
              }}
            >
              Cancel
            </Button>
            <Button 
              type="button" 
              size="sm"
              disabled={uploading || status === "success"}
              onClick={handleUpload}
            >
              {uploading && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
              Upload File
            </Button>
          </div>
        </div>
      )}
    </div>
  );
}
