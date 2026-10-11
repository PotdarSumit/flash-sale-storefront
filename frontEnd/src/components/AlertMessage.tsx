import { joinClassNames } from "@/api/client";

type AlertTone = "success" | "error" | "info";

interface AlertMessageProps {
  tone: AlertTone;
  title?: string;
  message: string;
}

const toneStyles: Record<AlertTone, string> = {
  success: "border-emerald-200 bg-emerald-50 text-emerald-900",
  error: "border-rose-200 bg-rose-50 text-rose-900",
  info: "border-sky-200 bg-sky-50 text-sky-900",
};

export function AlertMessage({ tone, title, message }: AlertMessageProps) {
  return (
    <div className={joinClassNames("rounded-2xl border px-4 py-3 shadow-sm", toneStyles[tone])}>
      {title ? <p className="text-sm font-semibold">{title}</p> : null}
      <p className={joinClassNames("text-sm", title ? "mt-1" : "")}>{message}</p>
    </div>
  );
}
