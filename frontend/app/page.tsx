import { CtaSection } from "@/components/home/cta-section";
import { FaqSection } from "@/components/home/faq-section";
import { FeaturesSection } from "@/components/home/features-section";
import { HeroSection } from "@/components/home/hero-section";
import { ToolsSection } from "@/components/home/tools-section";
import { MainLayout } from "@/components/layout/main-layout";

export default function HomePage() {
  return (
    <MainLayout>
      <HeroSection />
      <ToolsSection />
      <FeaturesSection />
      <FaqSection />
      <CtaSection />
    </MainLayout>
  );
}
