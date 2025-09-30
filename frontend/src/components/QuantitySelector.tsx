interface QuantitySelectorProps {
  quantity: number;
  onChange: (newQuantity: number) => void;
  min?: number;
  max?: number;
}

export function QuantitySelector({
  quantity,
  onChange,
  min = 0,
  max = 50,
}: QuantitySelectorProps) {
  const handleDecrement = () => {
    if (quantity > min) {
      onChange(quantity - 1);
    }
  };

  const handleIncrement = () => {
    if (quantity < max) {
      onChange(quantity + 1);
    }
  };

  return (
    <div className="flex items-center gap-2">
      <button
        type="button"
        onClick={handleDecrement}
        disabled={quantity <= min}
        aria-label="Decrease quantity"
        className="w-8 h-8 flex items-center justify-center rounded-full border-2 border-blue-600 text-blue-600 hover:bg-blue-50 disabled:border-gray-300 disabled:text-gray-300 disabled:hover:bg-transparent disabled:cursor-not-allowed transition-colors"
      >
        <span className="text-xl leading-none">−</span>
      </button>
      <span className="text-lg font-semibold min-w-[2rem] text-center">
        {quantity}
      </span>
      <button
        type="button"
        onClick={handleIncrement}
        disabled={quantity >= max}
        aria-label="Increase quantity"
        className="w-8 h-8 flex items-center justify-center rounded-full border-2 border-blue-600 text-blue-600 hover:bg-blue-50 disabled:border-gray-300 disabled:text-gray-300 disabled:hover:bg-transparent disabled:cursor-not-allowed transition-colors"
      >
        <span className="text-xl leading-none">+</span>
      </button>
    </div>
  );
}