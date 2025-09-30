interface CartIconProps {
  itemCount: number;
  onClick: () => void;
}

export function CartIcon({ itemCount, onClick }: CartIconProps) {
  return (
    <button
      type="button"
      onClick={onClick}
      aria-label="Shopping cart"
      className="relative p-2 hover:bg-gray-100 rounded-lg transition-colors"
    >
      {/* Cart icon SVG */}
      <svg
        xmlns="http://www.w3.org/2000/svg"
        className="h-8 w-8 text-gray-700"
        fill="none"
        viewBox="0 0 24 24"
        stroke="currentColor"
        strokeWidth={2}
      >
        <path
          strokeLinecap="round"
          strokeLinejoin="round"
          d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z"
        />
      </svg>

      {/* Item count badge */}
      <span
        className={`absolute -top-1 -right-1 flex items-center justify-center min-w-[1.5rem] h-6 px-1 text-xs font-bold text-white rounded-full ${
          itemCount > 0 ? 'bg-red-500' : 'bg-gray-400'
        }`}
      >
        {itemCount}
      </span>
    </button>
  );
}