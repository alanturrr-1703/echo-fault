interface StatusBadgeProps {
  status: string;
}

const statusStyles: Record<string, string> = {
  disconnected: 'bg-gray-700 text-gray-300',
  connecting: 'bg-yellow-900/50 text-yellow-300',
  connected: 'bg-blue-900/50 text-blue-300',
  streaming: 'bg-emerald-900/50 text-emerald-300',
  error: 'bg-red-900/50 text-red-300',
};

export function StatusBadge({ status }: StatusBadgeProps) {
  const style = statusStyles[status] ?? statusStyles.disconnected;
  return (
    <span className={`inline-flex items-center gap-2 rounded-full px-3 py-1 text-xs font-medium ${style}`}>
      <span className={`h-2 w-2 rounded-full ${status === 'streaming' ? 'animate-pulse bg-emerald-400' : 'bg-current opacity-60'}`} />
      {status.charAt(0).toUpperCase() + status.slice(1)}
    </span>
  );
}
