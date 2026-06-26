import { useState, useEffect, useCallback } from 'react';

export const useProfileManagement = (user: any) => {
  const [avatarUrlInput, setAvatarUrlInput] = useState('');
  const [avatarFileName, setAvatarFileName] = useState('');
  const [displayNameInput, setDisplayNameInput] = useState('');
  const [isChangingEmail, setIsChangingEmail] = useState(false);
  const [newEmailInput, setNewEmailInput] = useState('');
  const [otpSent, setOtpSent] = useState(false);
  const [otpInput, setOtpInput] = useState('');
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmNewPassword, setConfirmNewPassword] = useState('');

  const [showCurrentPassword, setShowCurrentPassword] = useState(false);
  const [showNewPassword, setShowNewPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

  const [profileStatus, setProfileStatus] = useState<{ type: 'success' | 'error'; message: string } | null>(null);
  const [emailStatus, setEmailStatus] = useState<{ type: 'success' | 'error'; message: string } | null>(null);
  const [passwordStatus, setPasswordStatus] = useState<{ type: 'success' | 'error'; message: string } | null>(null);

  useEffect(() => {
    if (user) {
      setAvatarUrlInput(user.avatar || '');
      setDisplayNameInput(user.name || '');
      setAvatarFileName('');
    }
  }, [user]);

  const handlePasswordChange = useCallback((val: string, setter: (v: string) => void) => {
    const filtered = val.replace(/[^\x21-\x7E]/g, '');
    setter(filtered);
  }, []);

  const handleAvatarFileChange = useCallback((e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      if (file.size > 2 * 1024 * 1024) {
        setProfileStatus({ type: 'error', message: 'Image size must be less than 2MB.' });
        return;
      }
      setAvatarFileName(file.name);
      const reader = new FileReader();
      reader.onload = (event) => {
        const result = event.target?.result;
        if (typeof result === 'string') {
          setAvatarUrlInput(result);
        }
      };
      reader.readAsDataURL(file);
    }
  }, []);

  const handleSendOtp = useCallback((e: React.FormEvent) => {
    e.preventDefault();
    if (!newEmailInput.trim()) {
      setEmailStatus({ type: 'error', message: 'New email cannot be empty.' });
      return;
    }
    if (!/\S+@\S+\.\S+/.test(newEmailInput)) {
      setEmailStatus({ type: 'error', message: 'Invalid email address format.' });
      return;
    }
    if (newEmailInput === user?.email) {
      setEmailStatus({ type: 'error', message: 'New email must be different from current email.' });
      return;
    }
    setOtpSent(true);
    setEmailStatus({ type: 'success', message: 'OTP verification code sent to your new email!' });
    setTimeout(() => setEmailStatus(null), 4000);
  }, [newEmailInput, user?.email]);

  return {
    avatarUrlInput,
    setAvatarUrlInput,
    avatarFileName,
    setAvatarFileName,
    displayNameInput,
    setDisplayNameInput,
    isChangingEmail,
    setIsChangingEmail,
    newEmailInput,
    setNewEmailInput,
    otpSent,
    setOtpSent,
    otpInput,
    setOtpInput,
    currentPassword,
    setCurrentPassword,
    newPassword,
    setNewPassword,
    confirmNewPassword,
    setConfirmNewPassword,
    showCurrentPassword,
    setShowCurrentPassword,
    showNewPassword,
    setShowNewPassword,
    showConfirmPassword,
    setShowConfirmPassword,
    profileStatus,
    setProfileStatus,
    emailStatus,
    setEmailStatus,
    passwordStatus,
    setPasswordStatus,
    handlePasswordChange,
    handleAvatarFileChange,
    handleSendOtp
  };
};

