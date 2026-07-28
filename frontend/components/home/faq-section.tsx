import { Accordion, type AccordionItem } from "@/components/ui/accordion";

const faqItems: AccordionItem[] = [
  { question: "What can I do with Convertexxx?", answer: "Convertexxx is being prepared for common PDF and image conversion tasks, including merging, splitting, compression, and format conversion." },
  { question: "Do I need to create an account?", answer: "The current interface is designed to keep the experience simple. Account features are not part of this initial foundation." },
  { question: "Which file formats will be supported?", answer: "Planned workflows focus on PDFs and common image formats such as JPG and PNG." },
  { question: "Will my documents remain private?", answer: "Privacy is a core product principle. File processing behavior will be clearly communicated as document workflows are introduced." },
  { question: "Can I use Convertexxx on mobile?", answer: "Yes. The interface is responsive and designed to work comfortably across phones, tablets, and desktop screens." },
  { question: "When will conversion tools be available?", answer: "This release establishes the user interface foundation. Conversion workflows will be introduced in a future phase." }
];

export function FaqSection() {
  return (
    <section id="faq" className="scroll-mt-20 border-t border-slate-800 bg-slate-900/30 px-4 py-20 sm:px-6 lg:px-8">
      <div className="mx-auto grid max-w-7xl gap-10 lg:grid-cols-[0.75fr_1.25fr] lg:gap-16">
        <div>
          <p className="text-sm font-semibold uppercase tracking-[0.18em] text-sky-300">FAQ</p>
          <h2 className="mt-3 text-3xl font-semibold tracking-tight text-white sm:text-4xl">Questions, answered simply.</h2>
          <p className="mt-4 max-w-md leading-7 text-slate-400">A quick overview of the product direction and the experience we are building.</p>
        </div>
        <Accordion items={faqItems} />
      </div>
    </section>
  );
}
