/** @type {import('tailwindcss').Config} */
export default {
  content: ["./index.html", "./src/**/*.{js,ts,jsx,tsx}"],
  darkMode: "class",
  theme: {
    extend: {
      colors: {
        "primary": "#F36F21",
        "primary-hover": "#d95f19",
        "primary-light": "#fce2d3",
        "brand-blue": "#12284C",
        "brand-blue-light": "#1c3d73",
        "brand-green": "#46A040",
        "brand-green-hover": "#3b8735",
        "brand-green-light": "#eef7ee",
        "surface": "#ffffff",
        "surface-gray": "#f8f9fa",
        "text-main": "#12284C",
        "text-muted": "#64748b",
        "outline-variant": "#cbd5e1",
        "error": "#dc2626",
        "error-container": "#fee2e2",
        "on-error-container": "#991b1b",
        "warning-container": "#fef3c7",
        "on-warning-container": "#92400e"
      },
      borderRadius: {
        DEFAULT: "0.25rem",
        sm: "0.25rem",
        md: "0.75rem",
        lg: "0.5rem",
        xl: "0.75rem",
        full: "9999px"
      },
      spacing: {
        base: "8px",
        xs: "4px",
        sm: "8px",
        md: "16px",
        lg: "24px",
        xl: "32px",
        xxl: "48px",
        gutter: "24px",
        "margin-mobile": "16px",
        "margin-desktop": "64px"
      },
      fontFamily: {
        headline: ["Plus Jakarta Sans", "sans-serif"],
        display: ["Plus Jakarta Sans", "sans-serif"],
        body: ["Inter", "sans-serif"],
        label: ["Inter", "sans-serif"]
      },
      fontSize: {
        "display-lg": ["48px", { lineHeight: "56px", letterSpacing: "-0.02em", fontWeight: "800" }],
        "display-lg-mobile": ["36px", { lineHeight: "44px", letterSpacing: "-0.02em", fontWeight: "800" }],
        "headline-lg": ["32px", { lineHeight: "40px", letterSpacing: "-0.01em", fontWeight: "700" }],
        "headline-md": ["24px", { lineHeight: "32px", fontWeight: "700" }],
        "body-lg": ["18px", { lineHeight: "28px", fontWeight: "400" }],
        "body-md": ["16px", { lineHeight: "24px", fontWeight: "400" }],
        "label-md": ["14px", { lineHeight: "20px", letterSpacing: "0.05em", fontWeight: "600" }],
        "caption": ["12px", { lineHeight: "16px", fontWeight: "400" }]
      }
    }
  },
  plugins: [
    require("@tailwindcss/forms")
  ]
}
