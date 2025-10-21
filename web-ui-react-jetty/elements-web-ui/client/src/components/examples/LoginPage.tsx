import LoginPage from '../LoginPage';
import { AuthProvider } from '@/contexts/AuthContext';

export default function LoginPageExample() {
  return (
    <AuthProvider>
      <LoginPage />
    </AuthProvider>
  );
}
