import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useApp } from '../context/AppContext';
import { fetchCourseDetail } from '../services/courseService';

interface StarIcon {
  icon: string;
  fill: boolean;
}

interface CartItem {
  id: string;
  title: string;
  author: string;
  rating: number;
  reviewsCount: string;
  duration: string;
  lectures: number;
  level: string;
  price: number;
  originalPrice: number;
  discountPercentage: number;
  imageUrl: string;
  stars: StarIcon[];
}

export const ShoppingCart: React.FC = () => {
  const navigate = useNavigate();
  const { cart, removeFromCart, checkoutCart, user, clearCart, refreshBalance } = useApp();
  const [cartItems, setCartItems] = useState<CartItem[]>([]);
  const [loading, setLoading] = useState(false);
  const [couponCode, setCouponCode] = useState('');
  const [couponDiscount, setCouponDiscount] = useState(0); // extra discount in percent
  const [errorMessage, setErrorMessage] = useState('');
  const [successMessage, setSuccessMessage] = useState('');
  
  const walletBalance = user?.walletBalance || 0;

  useEffect(() => {
    if (user) {
      refreshBalance().catch(console.error);
    }
  }, [user?.id]);

  useEffect(() => {
    const loadCartItems = async () => {
      setLoading(true);
      try {
        const itemPromises = cart.map(id => fetchCourseDetail(id));
        const details = await Promise.all(itemPromises);
        
        const mappedItems: CartItem[] = details.map(course => {
          const stars: StarIcon[] = [];
          for (let i = 1; i <= 5; i++) {
            stars.push({
              icon: i <= course.averageRating ? 'star' : i - 0.5 <= course.averageRating ? 'star_half' : 'star_border',
              fill: i <= course.averageRating || i - 0.5 <= course.averageRating
            });
          }
          const originalPrice = course.price > 0 ? Math.round(course.price * 1.3) : 0; 
          const discountPercentage = originalPrice > 0 ? Math.round(((originalPrice - course.price) / originalPrice) * 100) : 0;
          
          return {
            id: course.id.toString(),
            title: course.title,
            author: course.instructorName || 'Unknown Instructor',
            rating: course.averageRating || 0,
            reviewsCount: course.totalReviews?.toString() || '0',
            duration: `${Math.max(1, Math.round((course.totalLessons || 10) * 1.5))}h`, 
            lectures: course.totalLessons || 0,
            level: course.categoryName || 'All Levels',
            price: course.price,
            originalPrice: originalPrice,
            discountPercentage: discountPercentage,
            imageUrl: course.thumbnailUrl,
            stars: stars
          };
        });
        setCartItems(mappedItems);
      } catch (error) {
        console.error("Failed to load cart items", error);
        setErrorMessage("Failed to load cart from server.");
      } finally {
        setLoading(false);
      }
    };
    
    if (cart.length > 0) {
      loadCartItems();
    } else {
      setCartItems([]);
    }
  }, [cart]);

  const subTotal = cartItems.reduce((sum, item) => sum + item.price, 0);
  
  // Calculate final total based on standard items price and the applied coupon discount
  const finalTotal = subTotal * (1 - couponDiscount / 100);

  const handleApplyCoupon = (e: React.FormEvent) => {
    e.preventDefault();
    if (couponCode.toUpperCase() === 'NONSTOP20') {
      setCouponDiscount(20);
      setErrorMessage('');
      setSuccessMessage('Coupon NONSTOP20 applied: 20% discount!');
    } else if (couponCode.trim() === '') {
      setErrorMessage('Please enter a coupon code.');
      setSuccessMessage('');
    } else {
      setErrorMessage('Invalid Coupon Code! Try "NONSTOP20"');
      setSuccessMessage('');
    }
  };

  const handleRemoveItem = (id: string) => {
    removeFromCart(id);
  };

  const handleCheckout = async () => {
    if (cartItems.length === 0) {
      setErrorMessage('Your cart is empty.');
      setSuccessMessage('');
      return;
    }

    if (!user) {
      setErrorMessage('Please login to checkout.');
      return;
    }

    if (walletBalance < finalTotal) {
      setErrorMessage('Insufficient wallet balance! Please deposit to proceed.');
      setSuccessMessage('');
      return;
    }

    const success = await checkoutCart(finalTotal, cartItems.map(item => ({ id: item.id, title: item.title, price: item.price })));
    if (success) {
      setSuccessMessage('Checkout successful! Courses have been added to your account.');
      setErrorMessage('');
      setTimeout(() => {
        navigate('/dashboard');
      }, 2500);
    } else {
      setErrorMessage('Checkout failed. Please try again later.');
    }
  };

  return (
    <div className="relative z-10 flex-grow w-full max-w-[1280px] mx-auto px-margin-mobile md:px-margin-desktop py-xxl pt-8 flex flex-col gap-xl text-left">
      {/* Decorative Badge */}
      <div className="inline-flex items-center gap-1.5 bg-[#fce2d3] border border-primary/20 px-3 py-1 rounded-full text-primary font-bold text-xs uppercase tracking-wider mb-3 shadow-sm relative z-10 w-fit">
        <span className="material-symbols-outlined text-xs icon-fill">shopping_cart</span> Cart Arena
      </div>
      
      <h1 className="text-display-lg-mobile md:text-display-lg font-display font-black leading-tight relative z-10">
        <span className="bg-gradient-to-r from-[#0114a7] to-[#2563eb] bg-clip-text text-transparent">Shopping</span>{' '}
        <span className="bg-gradient-to-r from-[#ff6000] to-[#ff8c42] bg-clip-text text-transparent">Cart</span>
      </h1>

      {successMessage && (
        <div className="bg-green-50 border border-brand-green/30 text-brand-green p-4 rounded-xl text-left font-bold flex items-center gap-2 animate-fade-in shadow-sm relative z-10">
          <span className="material-symbols-outlined text-[20px] icon-fill">check_circle</span>
          {successMessage}
        </div>
      )}

      {errorMessage && (
        <div className="bg-red-50 border border-red-200 text-red-600 p-4 rounded-xl text-left font-bold flex items-center gap-2 animate-fade-in shadow-sm relative z-10">
          <span className="material-symbols-outlined text-[20px]">error</span>
          {errorMessage}
        </div>
      )}

      {loading ? (
        <div className="bg-surface rounded-2xl border border-gray-150 p-12 text-center flex flex-col items-center justify-center gap-4 shadow-sm relative z-10">
          <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-primary mb-4"></div>
          <h3 className="font-display font-black text-xl text-brand-blue">Loading cart...</h3>
        </div>
      ) : cartItems.length === 0 && !successMessage ? (
        <div className="bg-surface rounded-2xl border border-gray-150 p-12 text-center flex flex-col items-center justify-center gap-4 shadow-sm relative z-10">
          <div className="w-16 h-16 rounded-full bg-gray-100 text-text-muted flex items-center justify-center">
            <span className="material-symbols-outlined text-4xl">shopping_cart_off</span>
          </div>
          <h3 className="font-display font-black text-xl text-brand-blue">Your cart is empty</h3>
          <p className="font-body text-sm text-text-muted max-w-sm">Discover top tech courses and find the knowledge that fits your goals.</p>
          <Link to="/courses" className="bg-primary hover:bg-primary-hover text-white font-bold text-sm uppercase px-6 py-3 rounded-xl transition-all shadow-md">
            Explore courses
          </Link>
        </div>
      ) : (
        <div className="flex flex-col lg:flex-row gap-gutter items-start">
          {/* Left Column: Cart Items */}
          <div className="w-full lg:w-[70%] space-y-md">
            <div className="flex justify-between items-center mb-2">
              <h2 className="text-xl font-bold text-text-main">{cartItems.length} Course{cartItems.length !== 1 ? 's' : ''} in Cart</h2>
              <button 
                onClick={clearCart}
                className="text-primary hover:text-primary-hover font-semibold underline text-sm transition-colors cursor-pointer"
              >
                Remove All
              </button>
            </div>
            {cartItems.map((item) => (
              <div 
                key={item.id} 
                className="bg-surface rounded-lg p-md shadow-sm border border-outline-variant/30 flex flex-col sm:flex-row gap-md items-start group hover:-translate-y-1 transition-transform duration-300 hover:shadow-md"
              >
                <div className="w-full sm:w-40 aspect-video sm:aspect-square rounded bg-surface-gray overflow-hidden relative shrink-0">
                  <img 
                    className="w-full h-full object-cover group-hover:scale-105 transition-transform" 
                    alt={item.title} 
                    src={item.imageUrl} 
                  />
                </div>
                <div className="flex-grow flex flex-col justify-between min-h-[160px]">
                  <div>
                    <h3 className="font-headline-md text-body-lg font-semibold text-text-main mb-xs">{item.title}</h3>
                    <p className="font-body-md text-caption text-text-muted mb-sm">by {item.author}</p>
                    <div className="flex items-center space-x-xs mb-sm">
                      <span className="font-label-md text-label-md text-text-main">{item.rating}</span>
                      <div className="flex text-[#f9bf02]">
                        {item.stars.map((star, idx) => (
                          <span 
                            key={idx} 
                            className={`material-symbols-outlined text-[16px] ${star.fill ? 'icon-fill' : ''}`}
                          >
                            {star.icon}
                          </span>
                        ))}
                      </div>
                      <span className="font-body-md text-caption text-text-muted">({item.reviewsCount})</span>
                    </div>
                    <div className="flex flex-wrap gap-sm font-body-md text-caption text-text-muted">
                      <span className="flex items-center gap-1">
                        <span className="material-symbols-outlined text-[14px]">menu_book</span> 
                        {item.lectures} lectures
                      </span>
                      <span>• {item.level}</span>
                    </div>
                  </div>
                  <div className="flex flex-wrap items-center gap-md mt-md text-sm">
                    <button 
                      onClick={() => handleRemoveItem(item.id)}
                      className="text-primary hover:text-primary-hover font-label-md text-caption underline cursor-pointer"
                    >
                      Remove
                    </button>
                  </div>
                </div>
                <div className="flex flex-col items-end shrink-0 sm:pl-md border-t sm:border-t-0 sm:border-l border-outline-variant/30 pt-md sm:pt-0 w-full sm:w-auto">
                  <span className="font-headline-md text-headline-md text-primary font-bold">
                    {item.price.toLocaleString('vi-VN')}đ
                  </span>
                </div>
              </div>
            ))}
          </div>

          {/* Right Column: Summary Sticky Card */}
          <div className="w-full lg:w-[30%] lg:sticky lg:top-24">
            <div className="bg-surface rounded-lg p-lg shadow-sm border border-outline-variant/30 space-y-md">
              <div className="space-y-sm">
                <div className="flex items-center justify-between pb-4 border-b border-gray-200 mb-4">
                  <div className="flex items-center gap-2 text-text-muted">
                    <span className="material-symbols-outlined text-[20px] text-primary">account_balance_wallet</span>
                    <span className="font-medium text-sm">My Wallet:</span>
                  </div>
                  <div className="flex items-center gap-3">
                    <div className="font-bold text-brand-green tracking-tight">
                      {walletBalance.toLocaleString('vi-VN')}đ
                    </div>
                    <Link 
                      to="/dashboard#deposit" 
                      className="bg-primary/10 text-primary hover:bg-primary/20 transition-colors px-2.5 py-1 rounded text-xs font-semibold whitespace-nowrap"
                    >
                      Deposit
                    </Link>
                  </div>
                </div>
                <div className="font-label-md text-body-md text-text-muted">Total Price:</div>
                <div className="font-display-lg-mobile text-display-lg-mobile font-bold text-text-main tracking-tight">
                  {finalTotal.toLocaleString('vi-VN')}đ
                </div>
              </div>
              
              <button 
                onClick={handleCheckout}
                className="w-full bg-primary hover:bg-primary-hover text-white font-label-md text-label-md py-3 rounded-lg shadow-sm hover:shadow-md transition-all duration-300 transform hover:-translate-y-0.5 flex justify-center items-center gap-sm font-bold cursor-pointer"
              >
                Checkout
              </button>
              
              <div className="pt-md border-t border-outline-variant/30">
                <form onSubmit={handleApplyCoupon} className="flex relative">
                  <input 
                    className="w-full bg-surface text-text-main font-body-md text-body-md rounded-l-md border border-outline-variant focus:border-primary focus:ring-1 focus:ring-primary px-sm py-2 outline-none" 
                    placeholder="Apply Coupon" 
                    type="text"
                    value={couponCode}
                    onChange={(e) => setCouponCode(e.target.value)}
                  />
                  <button 
                    className="bg-brand-blue hover:bg-brand-blue-light text-white rounded-r-md px-md flex items-center transition-colors cursor-pointer" 
                    type="submit"
                  >
                    <span className="material-symbols-outlined">chevron_right</span>
                  </button>
                </form>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
