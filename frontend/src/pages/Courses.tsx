import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { fetchCourses, type CourseListItemResponse, type CourseSearchRequestParams } from '../services/courseService';
import { useApp } from '../context/AppContext';

export const Courses: React.FC = () => {
  const { cart, addToCart, user } = useApp();
  const navigate = useNavigate();
  // Filter and Sort states
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('All');
  const [priceMin, setPriceMin] = useState(0);
  const [priceMax, setPriceMax] = useState(2000000);
  const [selectedRatings, setSelectedRatings] = useState<string[]>([]);
  const [instructorName, setInstructorName] = useState('');
  const [activeSorts, setActiveSorts] = useState<{ field: 'price' | 'rating' | 'enrolled' | 'date'; direction: 'asc' | 'desc' }[]>([]);
  const [currentPage, setCurrentPage] = useState(1);
  const [filtersOpen, setFiltersOpen] = useState(false);

  // API Response states
  const [courses, setCourses] = useState<CourseListItemResponse[]>([]);
  const [totalPages, setTotalPages] = useState(1);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string>('');

  const coursesPerPage = 12; // Matches backend page size

  // Reset page on search or filter change
  useEffect(() => {
    setCurrentPage(1);
  }, [searchTerm, selectedCategory, priceMin, priceMax, selectedRatings, instructorName]);

  // Categories list matching actual database seed data
  const categories = [
    { value: 'All', label: 'All Courses' },
    { value: 'Web Development', label: 'Web Development' },
    { value: 'Mobile Development', label: 'Mobile Development' },
    { value: 'Programming Languages', label: 'Programming Languages' },
    { value: 'Database & Cloud', label: 'Database & Cloud' }
  ];

  const categoryMap: Record<string, number> = {
    'Web Development': 1,
    'Mobile Development': 2,
    'Programming Languages': 3,
    'Database & Cloud': 4
  };

  // Default directions for sorting parameters
  const defaultDirections = {
    price: 'asc' as const,
    rating: 'desc' as const,
    enrolled: 'desc' as const,
    date: 'desc' as const
  };

  // Helper to check if a sort field is active
  const isSortActive = (field: 'price' | 'rating' | 'enrolled' | 'date') => {
    return activeSorts.some(s => s.field === field);
  };

  // Helper to get active direction
  const getSortDirection = (field: 'price' | 'rating' | 'enrolled' | 'date') => {
    const s = activeSorts.find(item => item.field === field);
    return s ? s.direction : null;
  };

  // Sorting cycling logic from courses.html
  const handleSortClick = (field: 'price' | 'rating' | 'enrolled' | 'date') => {
    setActiveSorts(prev => {
      const idx = prev.findIndex(s => s.field === field);
      if (idx === -1) {
        // State 1: Inactive -> Active Default
        return [{ field, direction: defaultDirections[field] }]; // Allow single active sort to match standard database queries
      } else {
        const next = [...prev];
        const current = next[idx];
        if (current.direction === defaultDirections[field]) {
          // State 2: Active Default -> Active Toggled
          current.direction = defaultDirections[field] === 'asc' ? 'desc' : 'asc';
          return next;
        } else {
          // State 3: Active Toggled -> Inactive (Remove)
          next.splice(idx, 1);
          return next;
        }
      }
    });
  };

  // Checkbox handlers
  const handleRatingChange = (rating: string) => {
    setSelectedRatings(prev =>
      prev.includes(rating) ? prev.filter(r => r !== rating) : [...prev, rating]
    );
  };

  // Dual price range slider constraints
  const handleMinSlider = (val: number) => {
    const gap = 50000;
    if (val >= priceMax - gap) {
      setPriceMin(priceMax - gap);
    } else {
      setPriceMin(val);
    }
  };

  const handleMaxSlider = (val: number) => {
    const gap = 50000;
    if (val <= priceMin + gap) {
      setPriceMax(priceMin + gap);
    } else {
      setPriceMax(val);
    }
  };

  // Filters Reset
  const clearAllFilters = () => {
    setSelectedRatings([]);
    setPriceMin(0);
    setPriceMax(2000000);
    setActiveSorts([]);
    setSearchTerm('');
    setInstructorName('');
  };

  const selectCategoryTab = (category: string) => {
    setSelectedCategory(category);
  };

  // Load courses dynamically from backend API
  useEffect(() => {
    const loadCourses = async () => {
      setLoading(true);
      setError(null);
      try {
        const categoryIds: number[] = [];
        if (selectedCategory !== 'All') {
          const catId = categoryMap[selectedCategory];
          if (catId) {
            categoryIds.push(catId);
          }
        }

        // Determine rating ranges
        let minRating: number | undefined = undefined;
        let maxRating: number | undefined = undefined;
        if (selectedRatings.length > 0) {
          const mins = selectedRatings.map(r => {
            if (r === '4-5') return 4.0;
            if (r === '3-4') return 3.0;
            if (r === '2-3') return 2.0;
            if (r === '1-2') return 1.0;
            return 0.0;
          });
          const maxs = selectedRatings.map(r => {
            if (r === '4-5') return 5.0;
            if (r === '3-4') return 4.0;
            if (r === '2-3') return 3.0;
            if (r === '1-2') return 2.0;
            return 1.0;
          });
          minRating = Math.min(...mins);
          maxRating = Math.max(...maxs);
        }

        // Determine sorting params
        const sortBy: string[] = [];
        const order: string[] = [];
        const sortByMap: Record<string, string> = {
          price: 'price',
          rating: 'averageRating',
          enrolled: 'totalEnrolled',
          date: 'createdAt'
        };

        if (activeSorts.length > 0) {
          activeSorts.forEach(s => {
            const mappedField = sortByMap[s.field];
            if (mappedField) {
              sortBy.push(mappedField);
              order.push(s.direction);
            }
          });
        }

        const params: CourseSearchRequestParams = {
          keyword: searchTerm,
          categoryIds: categoryIds.length > 0 ? categoryIds : undefined,
          minPrice: priceMin,
          maxPrice: priceMax,
          minRating,
          maxRating,
          instructorName: instructorName,
          page: currentPage - 1, // backend is 0-based index
          size: coursesPerPage,
          sortBy: sortBy.length > 0 ? sortBy : undefined,
          order: order.length > 0 ? order : undefined
        };

        const result = await fetchCourses(params);
        setCourses(result.content || []);
        setTotalPages(result.totalPages || 1);
      } catch (err: any) {
        setError(err.message || 'Đã xảy ra lỗi khi tải danh sách khóa học');
      } finally {
        setLoading(false);
      }
    };

    loadCourses();
  }, [searchTerm, selectedCategory, priceMin, priceMax, selectedRatings, instructorName, activeSorts, currentPage]);

  const visibleCourses = courses;

  // Total active filters count for badge
  const activeFiltersCount =
    selectedRatings.length +
    (instructorName.trim() !== '' ? 1 : 0) +
    (priceMin > 0 || priceMax < 2000000 ? 1 : 0);

  // Helper styles for price slider
  const minPercent = (priceMin / 2000000) * 100;
  const maxPercent = 100 - ((priceMax / 2000000) * 100);

  // Helpers for card header styling
  const getCardBg = (id: number) => {
    switch (id % 4) {
      case 0:
        return 'bg-brand-blue';
      case 1:
        return 'bg-brand-blue-light';
      case 2:
        return 'bg-gray-200';
      case 3:
        return 'bg-primary-light';
      default:
        return 'bg-brand-blue';
    }
  };

  const getIconColor = (id: number) => {
    const bg = getCardBg(id);
    return bg === 'bg-brand-blue' || bg === 'bg-brand-blue-light'
      ? 'text-white'
      : 'text-brand-blue';
  };

  const getIconName = (categoryName?: string, title?: string) => {
    const cat = categoryName?.toLowerCase() || '';
    const t = title?.toLowerCase() || '';
    
    if (cat.includes('web') || t.includes('web') || t.includes('html') || t.includes('react') || t.includes('css')) {
      return 'code';
    }
    if (cat.includes('mobile') || t.includes('mobile') || t.includes('flutter') || t.includes('android') || t.includes('ios')) {
      return 'smartphone';
    }
    if (cat.includes('database') || cat.includes('cloud') || t.includes('sql') || t.includes('db') || t.includes('aws') || t.includes('docker') || t.includes('postgres')) {
      return 'dns';
    }
    if (t.includes('java')) {
      return 'settings_input_component';
    }
    if (t.includes('c++') || t.includes('cpp')) {
      return 'data_object';
    }
    if (t.includes('python')) {
      return 'terminal';
    }
    return 'school';
  };

  const handlePageChange = (page: number) => {
    setCurrentPage(page);
    const gridContainer = document.getElementById('courses-grid-container');
    if (gridContainer) {
      gridContainer.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }
  };

  return (
    <div className="relative z-10 flex-grow w-full max-w-[1440px] mx-auto px-4 lg:px-8 py-xxl pt-8 text-left">
      {/* Dynamic Alerts */}
      {successMessage && (
        <div className="fixed top-20 right-8 bg-brand-green border border-brand-green/30 text-white p-4 rounded-xl z-50 font-bold flex items-center gap-2 animate-fade-in shadow-xl">
          <span className="material-symbols-outlined text-[20px] icon-fill">check_circle</span>
          {successMessage}
        </div>
      )}

      {/* Page Header */}
      <header className="mb-xl">
        {/* Decorative Badge */}
        <div className="inline-flex items-center gap-1.5 bg-primary-light border border-primary/20 px-3.5 py-1.5 rounded-full text-primary font-extrabold text-xs uppercase tracking-wider mb-3 shadow-sm relative z-10">
          <span className="material-symbols-outlined text-xs icon-fill">local_library</span> Learning Arena
        </div>
        <h1 className="text-display-lg-mobile md:text-display-lg font-display font-black leading-tight relative z-10">
          <span className="bg-gradient-to-r from-[#0114a7] to-[#2563eb] bg-clip-text text-transparent">Explore</span>{' '}
          <span className="bg-gradient-to-r from-[#ff6000] to-[#ff8c42] bg-clip-text text-transparent">Courses</span>
        </h1>
        <p className="font-body text-base text-text-muted max-w-2xl leading-relaxed mt-1">Discover top-tier tech education modules designed to accelerate your career in software development.</p>
      </header>

      {/* Search & Advanced Filter Section */}
      <div className="flex flex-col gap-4 bg-surface p-5 rounded-2xl border border-gray-100 shadow-md mb-xl relative z-20">
        {/* Row 1: Action Controls Group (All Filters Button & Individual Sort Chips) */}
        <div className="flex flex-wrap items-center justify-between gap-4 w-full">
          <div className="flex flex-wrap items-center gap-2 md:gap-3 w-full">
            {/* All Filters Toggle Button */}
            <button
              onClick={() => setFiltersOpen(!filtersOpen)}
              className={`flex items-center justify-center gap-2 px-4 py-2.5 rounded-xl border text-xs md:text-sm font-semibold text-brand-blue shadow-sm transition-all shrink-0 select-none group ${
                filtersOpen ? 'bg-primary-light/40 border-primary text-primary' : 'border-gray-200 bg-surface hover:bg-surface-gray hover:border-primary'
              }`}
            >
              <span className="material-symbols-outlined text-[18px] md:text-[20px] text-text-muted group-hover:text-primary transition-colors">tune</span>
              <span>All Filters</span>
              {activeFiltersCount > 0 && (
                <span className="bg-primary text-white text-[10px] font-black w-5 h-5 rounded-full flex items-center justify-center shadow-sm">
                  {activeFiltersCount}
                </span>
              )}
            </button>

            {/* Vertical Divider */}
            <div className="h-6 w-px bg-gray-200 mx-1 hidden sm:block"></div>

            {/* Sorting Label */}
            <span className="text-[10px] md:text-xs font-bold text-brand-blue uppercase tracking-wider select-none hidden sm:inline-block">Sort by:</span>

            {/* Sort Button: Price */}
            <button
              onClick={() => handleSortClick('price')}
              className={`flex items-center gap-1.5 px-3.5 py-2.5 rounded-xl border text-xs font-semibold shadow-sm transition-all select-none hover:bg-surface-gray group ${
                isSortActive('price')
                  ? 'bg-primary-light/40 border-primary text-primary font-bold'
                  : 'bg-surface border-gray-200 text-brand-blue'
              }`}
            >
              <span>Price</span>
              <span
                className={`material-symbols-outlined text-[16px] transition-transform ${
                  isSortActive('price') ? 'text-primary' : 'text-text-muted'
                }`}
              >
                {getSortDirection('price') === 'asc'
                  ? 'arrow_upward'
                  : getSortDirection('price') === 'desc'
                  ? 'arrow_downward'
                  : 'unfold_more'}
              </span>
            </button>

            {/* Sort Button: Rated */}
            <button
              onClick={() => handleSortClick('rating')}
              className={`flex items-center gap-1.5 px-3.5 py-2.5 rounded-xl border text-xs font-semibold shadow-sm transition-all select-none hover:bg-surface-gray group ${
                isSortActive('rating')
                  ? 'bg-primary-light/40 border-primary text-primary font-bold'
                  : 'bg-surface border-gray-200 text-brand-blue'
              }`}
            >
              <span>Rating</span>
              <span
                className={`material-symbols-outlined text-[16px] transition-transform ${
                  isSortActive('rating') ? 'text-primary' : 'text-text-muted'
                }`}
              >
                {getSortDirection('rating') === 'asc'
                  ? 'arrow_upward'
                  : getSortDirection('rating') === 'desc'
                  ? 'arrow_downward'
                  : 'unfold_more'}
              </span>
            </button>

            {/* Sort Button: Popularity */}
            <button
              onClick={() => handleSortClick('enrolled')}
              className={`flex items-center gap-1.5 px-3.5 py-2.5 rounded-xl border text-xs font-semibold shadow-sm transition-all select-none hover:bg-surface-gray group ${
                isSortActive('enrolled')
                  ? 'bg-primary-light/40 border-primary text-primary font-bold'
                  : 'bg-surface border-gray-200 text-brand-blue'
              }`}
            >
              <span>Popularity</span>
              <span
                className={`material-symbols-outlined text-[16px] transition-transform ${
                  isSortActive('enrolled') ? 'text-primary' : 'text-text-muted'
                }`}
              >
                {getSortDirection('enrolled') === 'asc'
                  ? 'arrow_upward'
                  : getSortDirection('enrolled') === 'desc'
                  ? 'arrow_downward'
                  : 'unfold_more'}
              </span>
            </button>

            {/* Sort Button: Date */}
            <button
              onClick={() => handleSortClick('date')}
              className={`flex items-center gap-1.5 px-3.5 py-2.5 rounded-xl border text-xs font-semibold shadow-sm transition-all select-none hover:bg-surface-gray group ${
                isSortActive('date')
                  ? 'bg-primary-light/40 border-primary text-primary font-bold'
                  : 'bg-surface border-gray-200 text-brand-blue'
              }`}
            >
              <span>Release Date</span>
              <span
                className={`material-symbols-outlined text-[16px] transition-transform ${
                  isSortActive('date') ? 'text-primary' : 'text-text-muted'
                }`}
              >
                {getSortDirection('date') === 'asc'
                  ? 'arrow_upward'
                  : getSortDirection('date') === 'desc'
                  ? 'arrow_downward'
                  : 'unfold_more'}
              </span>
            </button>
          </div>
        </div>

        {/* Row 2: Search Input (Full Width below controls) */}
        <div className="relative w-full">
          <span className="material-symbols-outlined absolute left-4 top-1/2 -translate-y-1/2 text-text-muted text-[20px]">search</span>
          <input
            type="text"
            placeholder="Search for courses, keywords, or topics..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="w-full pl-12 pr-4 py-3.5 rounded-xl border border-gray-200 focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary text-sm shadow-inner transition-all bg-[#f8f9fa] outline-none"
          />
        </div>

        {/* Expanding Collapsible Glassmorphic Filter Drawer */}
        {filtersOpen && (
          <div className="overflow-hidden transition-all duration-300 border-t border-gray-100 mt-4 pt-4">
            <div className="grid grid-cols-1 md:grid-cols-3 gap-8 p-6 md:p-8 rounded-2xl bg-gradient-to-b from-[#f8f9fa]/50 to-white/30 border border-white/40 shadow-inner">
              {/* Column 1: Rating Ranges */}
              <div className="flex flex-col gap-3">
                <h4 className="text-xs md:text-sm font-black text-brand-blue uppercase tracking-wider mb-3 pb-1.5 border-b border-brand-blue/10 flex items-center gap-1.5">
                  <span className="material-symbols-outlined text-yellow-400 text-lg md:text-xl icon-fill">star</span> Rating Range
                </h4>
                <div className="flex flex-col gap-3">
                  {[
                    { value: '4-5', stars: '★★★★★', label: '4.0 - 5.0 Stars' },
                    { value: '3-4', stars: '★★★★☆', label: '3.0 - 4.0 Stars' },
                    { value: '2-3', stars: '★★★☆☆', label: '2.0 - 3.0 Stars' },
                    { value: '1-2', stars: '★★☆☆☆', label: '1.0 - 2.0 Stars' },
                    { value: '0-1', stars: '★☆☆☆☆', label: '0.0 - 1.0 Stars' }
                  ].map(item => (
                    <label key={item.value} className="flex items-center gap-3 cursor-pointer text-sm md:text-[15px] font-semibold text-text-main hover:text-primary transition-colors">
                      <input
                        type="checkbox"
                        checked={selectedRatings.includes(item.value)}
                        onChange={() => handleRatingChange(item.value)}
                        className="rounded border-gray-300 text-primary focus:ring-primary w-5 h-5 cursor-pointer"
                      />
                      <span className="flex items-center gap-1">
                        {item.label} <span className="text-yellow-400 text-sm md:text-[15px] flex items-center justify-center font-bold tracking-wide">{item.stars}</span>
                      </span>
                    </label>
                  ))}
                </div>
              </div>

              {/* Column 2: Instructor Search */}
              <div className="flex flex-col gap-3">
                <h4 className="text-xs md:text-sm font-black text-brand-blue uppercase tracking-wider mb-3 pb-1.5 border-b border-brand-blue/10 flex items-center gap-1.5">
                  <span className="material-symbols-outlined text-blue-400 text-lg md:text-xl">person</span> Instructor Name
                </h4>
                <div className="relative mt-2">
                  <span className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-text-muted text-[18px]">search</span>
                  <input
                    type="text"
                    placeholder="Search by instructor name..."
                    value={instructorName}
                    onChange={(e) => setInstructorName(e.target.value)}
                    className="w-full pl-10 pr-3 py-2.5 rounded-xl border border-gray-200 focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary text-sm shadow-sm transition-all bg-white outline-none"
                  />
                </div>
                <p className="text-xs text-text-muted mt-2 leading-relaxed">
                  Enter an instructor's name to fetch courses taught exclusively by them.
                </p>
              </div>

              {/* Column 3: Price Range Slider */}
              <div className="flex flex-col gap-3">
                <h4 className="text-xs md:text-sm font-black text-brand-blue uppercase tracking-wider mb-3 pb-1.5 border-b border-brand-blue/10 flex items-center gap-1.5">
                  <span className="material-symbols-outlined text-primary text-lg md:text-xl icon-fill">payments</span> Price Range
                </h4>
                <div className="flex flex-col gap-4 mt-1">
                  <div className="flex justify-center items-center">
                    <span className="bg-brand-blue text-white px-3.5 py-2 rounded-xl shadow-md text-sm md:text-[15px] font-extrabold tracking-wide border border-white/10 w-full text-center select-none">
                      from {priceMin === 0 ? '0đ' : priceMin.toLocaleString('en-US') + 'đ'} to {priceMax === 0 ? '0đ' : priceMax.toLocaleString('en-US') + 'đ'}
                    </span>
                  </div>

                  {/* Dual range slider component wrapper */}
                  <div className="price-slider-container relative mt-2 px-1">
                    {/* Slider Track background */}
                    <div className="absolute left-0 right-0 h-2 bg-gray-200 rounded-full"></div>
                    {/* Selected range track highlighting */}
                    <div
                      className="absolute h-2 bg-gradient-to-r from-primary to-[#ff8c42] rounded-full z-10"
                      style={{ left: `${minPercent}%`, right: `${maxPercent}%` }}
                    ></div>

                    {/* Min slider input */}
                    <input
                      type="range"
                      min="0"
                      max="2000000"
                      step="50000"
                      value={priceMin}
                      onChange={(e) => handleMinSlider(Number(e.target.value))}
                      className="z-30"
                    />
                    {/* Max slider input */}
                    <input
                      type="range"
                      min="0"
                      max="2000000"
                      step="50000"
                      value={priceMax}
                      onChange={(e) => handleMaxSlider(Number(e.target.value))}
                      className="z-30"
                    />
                  </div>

                  <div className="flex justify-between text-xs md:text-sm text-text-muted font-bold -mt-2">
                    <span>Min: 0đ</span>
                    <span>Max: 2,000,000đ</span>
                  </div>
                </div>
              </div>
            </div>

            {/* Footer Drawer Actions */}
            <div className="flex items-center justify-between border-t border-gray-100 mt-6 pt-4 px-2">
              <button
                onClick={clearAllFilters}
                className="flex items-center gap-2 text-sm md:text-base text-text-muted hover:text-primary font-bold transition-all px-4 py-2.5 rounded-xl hover:bg-surface-gray"
              >
                <span className="material-symbols-outlined text-[20px]">restart_alt</span> Reset Filters
              </button>

              <button
                onClick={() => setFiltersOpen(false)}
                className="flex items-center gap-1.5 px-6 py-2.5 bg-brand-blue hover:bg-brand-blue-light text-white font-extrabold text-sm md:text-base rounded-xl shadow-md transition-all transform hover:-translate-y-0.5"
              >
                Close &amp; Apply
              </button>
            </div>
          </div>
        )}

        {/* Row 2: Category Topic Tabs */}
        <div className="overflow-x-auto hide-scrollbar border-b border-gray-200 mt-2">
          <div className="flex gap-lg min-w-max pb-1">
            {categories.map(cat => (
              <button
                key={cat.value}
                onClick={() => selectCategoryTab(cat.value)}
                className={`category-tab pb-3 transition-all whitespace-nowrap text-xs md:text-sm ${
                  selectedCategory === cat.value
                    ? 'text-primary font-extrabold border-b-2 border-primary'
                    : 'text-text-muted hover:text-text-main font-semibold border-b-2 border-transparent'
                }`}
              >
                {cat.label}
              </button>
            ))}
          </div>
        </div>
      </div>

      {/* Course Grid container */}
      <section id="courses-grid-container" className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-gutter relative z-10 min-h-[400px]">
        {loading ? (
          <div className="flex flex-col items-center justify-center text-center p-12 col-span-1 sm:col-span-2 lg:col-span-3 fade-in">
            <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-primary mb-4"></div>
            <h3 className="text-base font-bold text-brand-blue">Loading Courses...</h3>
            <p className="text-xs text-text-muted mt-1">Retrieving latest learning modules from the server.</p>
          </div>
        ) : error ? (
          <div className="flex flex-col items-center justify-center text-center p-12 bg-red-50 rounded-2xl border border-red-100 col-span-1 sm:col-span-2 lg:col-span-3 fade-in">
            <span className="material-symbols-outlined text-red-500 text-5xl mb-2">error</span>
            <h3 className="text-base font-bold text-red-700">Error Loading Courses</h3>
            <p className="text-xs text-red-600 max-w-xs mt-1">{error}</p>
            <button 
              onClick={() => setCurrentPage(currentPage)}
              className="mt-4 px-4 py-2 bg-red-600 text-white rounded-lg text-xs font-bold hover:bg-red-700 transition-colors"
            >
              Try Again
            </button>
          </div>
        ) : visibleCourses.length === 0 ? (
          <div id="zero-state" className="flex flex-col items-center justify-center text-center p-12 bg-white rounded-2xl border border-gray-100 shadow-md col-span-1 sm:col-span-2 lg:col-span-3 fade-in">
            <span className="material-symbols-outlined text-text-muted text-5xl mb-2">find_in_page</span>
            <h3 className="text-base font-bold text-brand-blue">No Courses Match Your Filters</h3>
            <p className="text-xs text-text-muted max-w-xs mt-1">Try resetting some filters or modifying your search query to discover other options.</p>
          </div>
        ) : (
          visibleCourses.map(course => (
            <Link
              to={`/courses/${course.id}`}
              key={course.id}
              className="course-card bg-surface rounded-2xl overflow-hidden border border-gray-200 hover:-translate-y-1.5 hover:shadow-xl transition-all duration-300 flex flex-col cursor-pointer group shadow-sm text-left animate-fade-in"
            >
              <div className={`h-[160px] relative overflow-hidden flex items-center justify-center ${getCardBg(course.id)}`}>
                <img
                  alt={course.title}
                  className="absolute inset-0 w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
                  src={course.thumbnailUrl}
                />
              </div>
              <div className="p-5 flex-1 flex flex-col gap-3">
                <div className="flex items-center justify-between">
                  <span className="inline-block bg-surface-gray text-text-muted font-bold text-[10px] px-2.5 py-1 rounded-lg border border-gray-200">
                    {course.categoryName || 'General'}
                  </span>
                  {renderLevelBadge(course.price)}
                </div>
                <div>
                  <h3 className="font-display font-extrabold text-base text-brand-blue line-clamp-2 leading-snug group-hover:text-primary transition-colors min-h-[44px]">
                    {course.title}
                  </h3>
                  <p className="text-xs text-text-muted mt-1">{course.instructorName}</p>
                  <p className="text-xs text-text-muted line-clamp-2 mt-2 leading-relaxed min-h-[32px]">
                    {course.shortDescription}
                  </p>
                </div>
                <div className="flex items-center gap-1.5 mt-1 flex-wrap">
                  <span className="material-symbols-outlined text-yellow-400 text-[16px] icon-fill">star</span>
                  <span className="text-xs font-extrabold text-brand-blue">{course.averageRating}</span>
                  <span className="text-[10px] text-text-muted">({course.totalReviews.toLocaleString('en-US')} reviews)</span>
                  <span className="text-[10px] text-text-muted">•</span>
                  <span className="inline-flex items-center gap-0.5 text-[10px] text-text-muted">
                    <span className="material-symbols-outlined text-[12px] opacity-75">group</span>
                    {course.totalEnrolled.toLocaleString('en-US')} enrolled
                  </span>
                </div>
                {renderCardBottom(course)}
              </div>
            </Link>
          ))
        )}
      </section>

      {/* Pagination Component */}
      {!loading && !error && totalPages > 1 && (
        <div id="pagination-container" className="flex justify-center items-center gap-2 mt-12 mb-6 select-none animate-fade-in">
          {/* Previous button */}
          {currentPage === 1 ? (
            <button disabled className="w-10 h-10 rounded-full flex items-center justify-center border border-gray-100 text-gray-300 cursor-not-allowed bg-gray-50 transition-all duration-200">
              <span className="material-symbols-outlined text-[20px]">chevron_left</span>
            </button>
          ) : (
            <button
              onClick={() => handlePageChange(currentPage - 1)}
              className="w-10 h-10 rounded-full flex items-center justify-center border border-gray-200 text-brand-blue font-semibold hover:border-primary hover:text-primary transition-all duration-200 cursor-pointer bg-white shadow-sm"
            >
              <span className="material-symbols-outlined text-[20px]">chevron_left</span>
            </button>
          )}

          {/* Page numbers */}
          {Array.from({ length: totalPages }, (_, idx) => idx + 1).map(page => (
            page === currentPage ? (
              <button key={page} className="w-10 h-10 rounded-full flex items-center justify-center bg-primary text-white font-extrabold shadow-md transition-all duration-200">
                {page}
              </button>
            ) : (
              <button
                key={page}
                onClick={() => handlePageChange(page)}
                className="w-10 h-10 rounded-full flex items-center justify-center border border-gray-200 text-brand-blue font-semibold hover:border-primary hover:text-primary transition-all duration-200 cursor-pointer bg-white shadow-sm"
              >
                {page}
              </button>
            )
          ))}

          {/* Next button */}
          {currentPage === totalPages ? (
            <button disabled className="w-10 h-10 rounded-full flex items-center justify-center border border-gray-100 text-gray-300 cursor-not-allowed bg-gray-50 transition-all duration-200">
              <span className="material-symbols-outlined text-[20px]">chevron_right</span>
            </button>
          ) : (
            <button
              onClick={() => handlePageChange(currentPage + 1)}
              className="w-10 h-10 rounded-full flex items-center justify-center border border-gray-200 text-brand-blue font-semibold hover:border-primary hover:text-primary transition-all duration-200 cursor-pointer bg-white shadow-sm"
            >
              <span className="material-symbols-outlined text-[20px]">chevron_right</span>
            </button>
          )}
        </div>
      )}
    </div>
  );

  function renderLevelBadge(price: number) {
    if (price === 0) {
      return (
        <span className="inline-block bg-[#fce2d3] text-primary font-bold text-[10px] px-2.5 py-1 rounded-lg">
          Beginner
        </span>
      );
    } else if (price < 250000) {
      return (
        <span className="inline-block bg-blue-100 text-blue-800 font-bold text-[10px] px-2.5 py-1 rounded-lg">
          Intermediate
        </span>
      );
    } else {
      return (
        <span className="inline-block bg-[#ba1a1a]/10 text-[#ba1a1a] font-bold text-[10px] px-2.5 py-1 rounded-lg">
          Advanced
        </span>
      );
    }
  }

  function renderCardBottom(course: CourseListItemResponse) {
    const isFree = course.price === 0;

    if (course.enrolled) {
      const progress = course.progressPercentage || 0;
      return (
        <div className="mt-auto pt-3 border-t border-gray-100 flex flex-col gap-3">
          <div className="w-full flex flex-col gap-1 text-left">
            <div className="flex justify-between text-[10px] font-bold text-brand-blue">
              <span>Progress</span>
              <span>{progress}%</span>
            </div>
            <div className="w-full bg-gray-200 h-1.5 rounded-full overflow-hidden">
              <div 
                className="bg-brand-green h-full transition-all duration-300"
                style={{ width: `${progress}%` }}
              ></div>
            </div>
          </div>
          <button
            onClick={(e) => {
              e.preventDefault();
              e.stopPropagation();
              navigate('/dashboard');
            }}
            className="w-full py-2 bg-primary hover:bg-primary-hover text-white font-bold text-xs rounded-lg transition-colors shadow-sm flex items-center justify-center gap-1.5 cursor-pointer"
          >
            <span>Continue Learning</span>
            <span className="material-symbols-outlined text-[16px]">arrow_forward</span>
          </button>
        </div>
      );
    }

    return (
      <div className="mt-auto pt-3 border-t border-gray-100 flex flex-col gap-2">
        <div className="flex items-center justify-between">
          <div className="flex flex-col text-left">
            {isFree ? (
              <span className="font-display font-black text-brand-green text-base">Free</span>
            ) : (
              <span className="font-display font-black text-primary text-base">
                {course.price.toLocaleString('en-US')}đ
              </span>
            )}
          </div>
          {isFree ? (
            <span className="bg-brand-green/10 text-brand-green font-extrabold text-[10px] px-2 py-1 rounded shadow-sm">
              Get Free
            </span>
          ) : (
            <button
              onClick={(e) => {
                e.preventDefault();
                e.stopPropagation();
                if (!user) {
                  navigate('/login');
                  return;
                }
                addToCart(course.id.toString());
                setSuccessMessage('Course added to cart successfully!');
                setTimeout(() => setSuccessMessage(''), 3000);
              }}
              disabled={cart.includes(course.id.toString())}
              className={`w-8 h-8 rounded-full flex items-center justify-center transition-all shadow-sm ${
                cart.includes(course.id.toString())
                  ? 'bg-gray-200 text-gray-500 cursor-not-allowed'
                  : 'bg-primary text-white hover:bg-orange-600 hover:scale-105 hover:shadow-md cursor-pointer'
              }`}
              title={cart.includes(course.id.toString()) ? 'In Cart' : 'Add to cart'}
            >
              <span className="material-symbols-outlined text-[18px]">
                {cart.includes(course.id.toString()) ? 'check' : 'add'}
              </span>
            </button>
          )}
        </div>
      </div>
    );
  }
};
