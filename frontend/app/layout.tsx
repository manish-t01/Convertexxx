import type { Metadata } from "next";
import type { ReactNode } from "react";

import "./globals.css";

export const metadata: Metadata = {
  title: "Convertexxx",
  description: "A privacy-first document conversion platform."
};

type RootLayoutProperties = Readonly<{ children: ReactNode }>;

export default function RootLayout({ children }: RootLayoutProperties) {
  return (
    <html lang="en">
      <body>{children}</body>
    </html>
  );
}
