# KProfiles Frontend

A modern, full-featured KProfiles frontend for the LineageOS-based Xiaomi
`sweet` build maintained by [manipvlator](https://github.com/manipvlator).

This fork starts from the official KProfiles frontend, uses the HaloUI
Material 3 Compose interface, and adds the master switch, Battery Saver
handling, Settings search provider, and device-protected boot behavior from
TheParasiteProject.

## Features

- Material 3 Compose UI with light, dark, system, dynamic-color, and accent
  color options.
- Master switch to enable or disable KProfiles without losing the selected
  mode or automatic-mode preference.
- Four kernel modes: Disabled (`0`), Battery (`1`), Balanced (`2`), and
  Performance (`3`).
- Auto KProfiles: switches to Battery mode when the screen turns off and
  restores the selected mode when the screen turns on.
- Battery Saver integration: KProfiles temporarily uses Battery mode and
  disables automatic switching while Android Battery Saver is active.
- Quick Settings tile that cycles through the modes. The tile is unavailable
  while KProfiles is disabled or Battery Saver is active.
- Settings entry under Battery and a Settings search provider.
- Persistent service and `LOCKED_BOOT_COMPLETED` support using
  device-protected storage.
- Indonesian translations.

## Kernel integration

This frontend talks to the KProfiles 6.0 kernel interface:

```text
/sys/kernel/kprofiles/kp_mode
/sys/module/kprofiles/parameters/auto_kp
```

The paired Reforge kernel applies the modes to its kernel-side CPU input
boost, devfreq boost, and DRM atomic boost hooks. Battery mode skips those
boosts, Balanced mode limits display boosts to a short window after input, and
Disabled/Performance keep the stock always-boost behavior. Userspace power
HALs may still apply their own boosts independently.

Enable the backend in the kernel defconfig:

```text
CONFIG_KPROFILES=y
CONFIG_AUTO_KPROFILES=y
CONFIG_AUTO_KPROFILES_MSM_DRM=y
```

`auto_kp` is intentionally off until userspace enables it. The app writes it
on only when Auto KProfiles is enabled and Battery Saver is inactive;
otherwise it writes off.

## Build integration

The app is a privileged `system_ext` application signed with the platform
certificate. It cannot be sideloaded as a normal APK; build it into the ROM.

Clone this fork into the ROM source:

```shell
git clone https://github.com/manipvlator/android_packages_apps_Kprofiles.git \
    packages/apps/KProfiles
```

Add it to the common device tree:

```make
PRODUCT_PACKAGES += KProfiles

SYSTEM_EXT_PUBLIC_SEPOLICY_DIRS += packages/apps/KProfiles/sepolicy/public
SYSTEM_EXT_PRIVATE_SEPOLICY_DIRS += packages/apps/KProfiles/sepolicy/private
```

A local manifest entry can point at the `main` branch:

```xml
<project
    path="packages/apps/KProfiles"
    name="android_packages_apps_Kprofiles"
    remote="manipvlator"
    revision="main" />
```

The app also needs the KProfiles sysfs nodes to be writable by
`system_app`. The repository includes the required sepolicy and init rc.

## Usage

1. Build the app into the ROM and flash the ROM.
2. Open **Settings → Battery → KProfiles**, or use the Quick Settings tile.
3. Turn on **Use KProfiles**, then choose a mode.
4. Enable **Auto KProfiles** if you want automatic Battery mode while the
   screen is off.
5. Android Battery Saver temporarily overrides the selected mode with Battery
   mode and disables automatic switching.

KProfiles is disabled by default. The default mode is `0` (Disabled) and
automatic mode is off. Turn on **Use KProfiles** once, then choose a mode.

## Kernel-only control

For ROMs without the frontend, a root shell or kernel manager such as FKM can
write the same nodes:

```shell
# Battery mode
echo 1 > /sys/kernel/kprofiles/kp_mode

# Enable Auto KProfiles
echo Y > /sys/module/kprofiles/parameters/auto_kp
```

The master switch, Battery Saver override, and Quick Settings tile are
frontend features and are not available through the sysfs nodes alone.

## Credits

- [dakkshesh07/Kprofiles](https://github.com/dakkshesh07/Kprofiles) for the
  kernel interface.
- [KProfiles/android_packages_apps_Kprofiles](https://github.com/KProfiles/android_packages_apps_KProfiles)
  for the official frontend.
- [haloUI](https://github.com/haloUI) for the Material 3 Compose UI.
- [TheParasiteProject](https://github.com/TheParasiteProject) for the master
  switch, Battery Saver handling, and Settings search provider.
- CannedShroud and the LineageOS contributors for the original frontend work.

## License

Apache License 2.0.
