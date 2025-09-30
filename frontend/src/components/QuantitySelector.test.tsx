import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { QuantitySelector } from './QuantitySelector';

describe('QuantitySelector', () => {
  it('should render with initial quantity', () => {
    render(<QuantitySelector quantity={3} onChange={vi.fn()} />);

    expect(screen.getByText('3')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /decrease/i })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /increase/i })).toBeInTheDocument();
  });

  it('should call onChange when increment button clicked', async () => {
    const user = userEvent.setup();
    const handleChange = vi.fn();

    render(<QuantitySelector quantity={1} onChange={handleChange} />);

    const incrementButton = screen.getByRole('button', { name: /increase/i });
    await user.click(incrementButton);

    expect(handleChange).toHaveBeenCalledWith(2);
  });

  it('should call onChange when decrement button clicked', async () => {
    const user = userEvent.setup();
    const handleChange = vi.fn();

    render(<QuantitySelector quantity={5} onChange={handleChange} />);

    const decrementButton = screen.getByRole('button', { name: /decrease/i });
    await user.click(decrementButton);

    expect(handleChange).toHaveBeenCalledWith(4);
  });

  it('should disable decrement button when quantity is at minimum', () => {
    render(<QuantitySelector quantity={1} onChange={vi.fn()} min={1} />);

    const decrementButton = screen.getByRole('button', { name: /decrease/i });
    expect(decrementButton).toBeDisabled();
  });

  it('should disable increment button when quantity is at maximum', () => {
    render(<QuantitySelector quantity={50} onChange={vi.fn()} max={50} />);

    const incrementButton = screen.getByRole('button', { name: /increase/i });
    expect(incrementButton).toBeDisabled();
  });

  it('should not call onChange when decrement disabled', async () => {
    const user = userEvent.setup();
    const handleChange = vi.fn();

    render(<QuantitySelector quantity={1} onChange={handleChange} min={1} />);

    const decrementButton = screen.getByRole('button', { name: /decrease/i });
    await user.click(decrementButton);

    expect(handleChange).not.toHaveBeenCalled();
  });

  it('should not call onChange when increment disabled', async () => {
    const user = userEvent.setup();
    const handleChange = vi.fn();

    render(<QuantitySelector quantity={50} onChange={handleChange} max={50} />);

    const incrementButton = screen.getByRole('button', { name: /increase/i });
    await user.click(incrementButton);

    expect(handleChange).not.toHaveBeenCalled();
  });

  it('should use default min and max values', () => {
    render(<QuantitySelector quantity={25} onChange={vi.fn()} />);

    const decrementButton = screen.getByRole('button', { name: /decrease/i });
    const incrementButton = screen.getByRole('button', { name: /increase/i });

    expect(decrementButton).not.toBeDisabled();
    expect(incrementButton).not.toBeDisabled();
  });

  it('should display quantity with proper styling', () => {
    render(<QuantitySelector quantity={7} onChange={vi.fn()} />);

    const quantityDisplay = screen.getByText('7');
    expect(quantityDisplay).toHaveClass('text-lg', 'font-semibold', 'min-w-[2rem]');
  });
});