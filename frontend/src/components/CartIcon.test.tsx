import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { CartIcon } from './CartIcon';

describe('CartIcon', () => {
  it('should render cart icon with item count badge', () => {
    render(<CartIcon itemCount={5} onClick={vi.fn()} />);

    expect(screen.getByLabelText(/shopping cart/i)).toBeInTheDocument();
    expect(screen.getByText('5')).toBeInTheDocument();
  });

  it('should display zero when item count is 0', () => {
    render(<CartIcon itemCount={0} onClick={vi.fn()} />);

    expect(screen.getByText('0')).toBeInTheDocument();
  });

  it('should display large numbers correctly', () => {
    render(<CartIcon itemCount={99} onClick={vi.fn()} />);

    expect(screen.getByText('99')).toBeInTheDocument();
  });

  it('should call onClick when clicked', async () => {
    const user = userEvent.setup();
    const handleClick = vi.fn();

    render(<CartIcon itemCount={3} onClick={handleClick} />);

    const button = screen.getByRole('button', { name: /shopping cart/i });
    await user.click(button);

    expect(handleClick).toHaveBeenCalledOnce();
  });

  it('should be keyboard accessible', async () => {
    const user = userEvent.setup();
    const handleClick = vi.fn();

    render(<CartIcon itemCount={2} onClick={handleClick} />);

    const button = screen.getByRole('button', { name: /shopping cart/i });
    button.focus();
    await user.keyboard('{Enter}');

    expect(handleClick).toHaveBeenCalledOnce();
  });

  it('should highlight badge when item count is greater than 0', () => {
    const { container } = render(<CartIcon itemCount={5} onClick={vi.fn()} />);

    const badge = screen.getByText('5');
    expect(badge).toHaveClass('bg-red-500');
  });

  it('should show badge with different style when item count is 0', () => {
    const { container } = render(<CartIcon itemCount={0} onClick={vi.fn()} />);

    const badge = screen.getByText('0');
    expect(badge).toHaveClass('bg-gray-400');
  });
});