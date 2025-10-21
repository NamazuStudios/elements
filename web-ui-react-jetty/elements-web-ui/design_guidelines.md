# Design Guidelines: Elements Admin Editor

## Design Approach

**Selected Approach:** Design System + Modern Admin Dashboard Patterns

**Rationale:** As a utility-focused admin tool for managing backend resources, this interface prioritizes clarity, efficiency, and data management capabilities. Drawing inspiration from Linear, Vercel Admin, and Retool for clean, modern admin interfaces that balance functionality with visual polish.

**Key Principles:**
- Information hierarchy over decoration
- Efficient data scanning and manipulation
- Clear visual feedback for all actions
- Consistent, predictable interactions

## Core Design Elements

### A. Color Palette

**Dark Mode Primary (Default):**
- Background Base: `222 8% 9%` (near-black with slight warmth)
- Surface Elevated: `222 8% 12%` (cards, panels)
- Surface Hover: `222 8% 15%` (interactive surfaces)
- Border Subtle: `222 8% 18%` (dividers, outlines)
- Border Default: `222 8% 25%` (input borders)

**Text Colors:**
- Primary Text: `222 8% 95%` (main content)
- Secondary Text: `222 8% 65%` (labels, metadata)
- Muted Text: `222 8% 45%` (placeholders, disabled)

**Brand & Accent:**
- Primary Brand: `217 91% 60%` (bright blue for CTAs, links)
- Primary Hover: `217 91% 55%` (button hover states)
- Success: `142 76% 36%` (confirmations, success states)
- Warning: `38 92% 50%` (alerts, important notices)
- Error: `0 84% 60%` (errors, destructive actions)

**Light Mode (for future implementation):**
- Background: `0 0% 100%`
- Surface: `220 13% 98%`
- Borders: `220 13% 91%`

### B. Typography

**Font Family:**
- Primary: Inter (via Google Fonts) - clean, legible, excellent for data
- Monospace: JetBrains Mono (for code, IDs, technical values)

**Type Scale:**
- Page Titles: `text-2xl font-semibold` (30px)
- Section Headers: `text-xl font-semibold` (20px)
- Card Titles: `text-base font-medium` (16px)
- Body Text: `text-sm` (14px)
- Labels: `text-xs font-medium uppercase tracking-wide` (12px)
- Table Data: `text-sm` (14px)
- Metadata: `text-xs text-muted` (12px)

### C. Layout System

**Spacing Primitives:** Use Tailwind units of 2, 4, 6, 8, 12, 16, 24 for consistent rhythm

**Grid Structure:**
- Sidebar Navigation: Fixed `w-64` (256px)
- Main Content: `flex-1 max-w-7xl mx-auto`
- Page Padding: `p-6` on mobile, `p-8` on desktop
- Card Spacing: `gap-6` for card grids
- Form Fields: `space-y-4` vertical rhythm

**Responsive Breakpoints:**
- Mobile: Collapse sidebar to overlay/drawer
- Tablet (md): Show compact sidebar
- Desktop (lg+): Full sidebar with icons + labels

### D. Component Library

**Authentication Screen:**
- Centered card (max-w-md) with elevated surface
- Company logo/wordmark at top
- Session token input with secure masking option
- SUPERUSER level badge display after validation
- Clean error messaging below input
- Minimal, focused design - no distractions

**Dashboard Layout:**
- Fixed sidebar: Resource navigation with icons (Lucide icons)
- Top bar: Breadcrumbs, user info, logout action
- Main area: Page title + action buttons + content grid
- Stats cards: 2-4 column grid showing key metrics (total users, applications, etc.)

**Data Tables:**
- Sticky header with column sorting indicators
- Striped rows (`even:bg-surface-elevated`) for readability
- Row hover states with subtle highlight
- Action buttons (edit, delete) aligned right
- Pagination controls at bottom
- Search/filter bar above table
- Empty states with helpful guidance

**Forms (CRUD Operations):**
- Modal overlays (max-w-2xl) for create/edit operations
- Field groups with clear labels (text-xs uppercase)
- Input styling: `bg-surface border border-default focus:border-primary`
- Inline validation with error messages
- Submit/Cancel button pair (primary + ghost)
- Loading states for async operations

**Navigation Sidebar:**
- Grouped resources by category (Auth, Game, Commerce, etc.)
- Active state: `bg-surface-hover border-l-2 border-primary`
- Icon + label layout
- Collapse/expand capability
- Logout button at bottom

**Cards & Containers:**
- Default card: `bg-surface-elevated rounded-lg border border-subtle p-6`
- Hover cards: Add `hover:border-default transition-colors`
- Section dividers: `border-t border-subtle pt-6`

### E. Interaction Patterns

**Animations:** Minimal, purposeful only
- Page transitions: Simple fade (150ms)
- Modal entry: Scale + fade (200ms)
- Dropdown menus: Slide down (150ms)
- Loading spinners: Subtle rotation for async actions
- NO scroll-triggered animations
- NO decorative transitions

**State Feedback:**
- Loading: Skeleton screens for initial loads, spinners for actions
- Success: Green toast notification (bottom-right, 3s duration)
- Errors: Red toast with error message + retry option
- Empty states: Centered icon + message + CTA

**Button Variants:**
- Primary: `bg-primary hover:bg-primary-hover text-white`
- Secondary: `bg-surface-hover hover:bg-surface border border-default`
- Ghost: `hover:bg-surface-hover text-secondary`
- Destructive: `bg-error hover:bg-error/90 text-white`

## Accessibility & Quality Standards

- All interactive elements have focus states (`focus:ring-2 ring-primary/20`)
- Form inputs have proper labels (not just placeholders)
- Color contrast meets WCAG AA standards (especially in dark mode)
- Keyboard navigation for all critical flows
- ARIA labels for icon-only buttons
- Loading states prevent interaction during async operations

## Icons

Use **Lucide Icons** (via CDN) for all interface icons:
- Navigation: Menu, ChevronRight, Settings, LogOut
- Resources: Users, Package, Database, Shield, Code
- Actions: Plus, Edit, Trash2, Save, X
- States: CheckCircle, AlertTriangle, Info, Loader

## Key Screens

1. **Login:** Centered card with session token input
2. **Dashboard:** Stats overview + recent activity
3. **Resource List:** Searchable table with filters + bulk actions
4. **Resource Detail/Edit:** Modal form with validation
5. **Empty State:** When no resources exist, clear CTA to create

This design prioritizes developer/admin efficiency with clear information architecture, fast data access, and reliable CRUD operations.