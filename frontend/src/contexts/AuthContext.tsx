import {
  createContext,
  useContext,
  useState,
  useCallback,
  type ReactNode,
} from "react";
import type { Usuario, LoginDTO, CreateUsuarioDTO } from "@/types";
import { authService } from "@/services/authService";

interface AuthContextType {
  user: Usuario | null;
  isAuthenticated: boolean;
  loading: boolean;
  login: (credentials: LoginDTO) => Promise<void>;
  register: (data: CreateUsuarioDTO) => Promise<void>;
  updateUser: (user: Usuario) => void;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | null>(null);

const STORAGE_KEY = "disconnect_user";

function getStoredUser(): Usuario | null {
  const stored = sessionStorage.getItem(STORAGE_KEY);
  if (!stored) return null;
  try {
    return JSON.parse(stored) as Usuario;
  } catch {
    sessionStorage.removeItem(STORAGE_KEY);
    return null;
  }
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<Usuario | null>(getStoredUser);
  const [loading, setLoading] = useState(false);

  const login = useCallback(async (credentials: LoginDTO) => {
    setLoading(true);
    try {
      const loggedUser = await authService.login(credentials);
      sessionStorage.setItem(STORAGE_KEY, JSON.stringify(loggedUser));
      setUser(loggedUser);
    } finally {
      setLoading(false);
    }
  }, []);

  const register = useCallback(async (data: CreateUsuarioDTO) => {
    setLoading(true);
    try {
      const newUser = await authService.register(data);
      sessionStorage.setItem(STORAGE_KEY, JSON.stringify(newUser));
      setUser(newUser);
    } finally {
      setLoading(false);
    }
  }, []);

  const updateUser = useCallback((updatedUser: Usuario) => {
    setUser((currentUser) => {
      const userWithToken = updatedUser.token
        ? updatedUser
        : { ...updatedUser, token: currentUser?.token };

      sessionStorage.setItem(STORAGE_KEY, JSON.stringify(userWithToken));
      return userWithToken;
    });
  }, []);

  const logout = useCallback(() => {
    sessionStorage.removeItem(STORAGE_KEY);
    setUser(null);
  }, []);

  return (
    <AuthContext.Provider
      value={{
        user,
        isAuthenticated: user !== null,
        loading,
        login,
        register,
        updateUser,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth(): AuthContextType {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return context;
}
