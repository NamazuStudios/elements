import StatsCard from '../StatsCard';
import { Users, Package, Database, Activity } from 'lucide-react';

export default function StatsCardExample() {
  return (
    <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4 p-6 bg-background">
      <StatsCard
        title="Total Users"
        value="2,543"
        description="Active users"
        icon={Users}
        trend={{ value: "12%", isPositive: true }}
      />
      <StatsCard
        title="Applications"
        value="48"
        description="Registered apps"
        icon={Package}
      />
      <StatsCard
        title="Inventory Items"
        value="1,284"
        description="Total items"
        icon={Database}
        trend={{ value: "8%", isPositive: true }}
      />
      <StatsCard
        title="API Requests"
        value="45.2K"
        description="Last 24 hours"
        icon={Activity}
      />
    </div>
  );
}
