# LibreOffice Installation Guide for Convertexxx

The `DocumentConverter` requires **LibreOffice** to be installed on the system where the backend runs. LibreOffice is executed in headless mode (no GUI) to convert office documents to PDF.

## Supported Conversions

| Input Format | Output Format |
|--------------|---------------|
| doc, docx, odt, rtf | pdf |
| ppt, pptx, odp | pdf |
| xls, xlsx, ods | pdf |

---

## Configuration

The LibreOffice executable path is configured in `application.yml`:

```yaml
app:
  libreoffice:
    executable: soffice
```

### Platform-specific examples

| Platform | Value |
|----------|-------|
| Linux / macOS (on PATH) | `soffice` |
| Windows (on PATH) | `soffice.exe` |
| Windows (full path) | `C:\Program Files\LibreOffice\program\soffice.exe` |

---

## Installation

### Windows

1. Download the installer from [https://www.libreoffice.org/download](https://www.libreoffice.org/download)
2. Run the installer and complete the setup
3. Update `application.yml`:
   ```yaml
   app:
     libreoffice:
       executable: C:\Program Files\LibreOffice\program\soffice.exe
   ```
4. Alternatively, add `C:\Program Files\LibreOffice\program` to your system `PATH` and use `soffice.exe`.

**Verify installation:**
```cmd
"C:\Program Files\LibreOffice\program\soffice.exe" --version
```

### Linux (Debian / Ubuntu)

```bash
sudo apt update
sudo apt install libreoffice
```

**Verify installation:**
```bash
soffice --version
```

### Linux (RHEL / Fedora / CentOS)

```bash
sudo dnf install libreoffice
```

### macOS

Using Homebrew:
```bash
brew install --cask libreoffice
```

**Verify installation:**
```bash
soffice --version
```

---

## Troubleshooting

### Conversion fails with "Cannot run program soffice"

LibreOffice is not found at the configured path. Ensure:

1. LibreOffice is installed
2. The `app.libreoffice.executable` property in `application.yml` points to the correct binary
3. On Linux/macOS, run `which soffice` to verify the path
4. On Windows, verify the full path exists: `C:\Program Files\LibreOffice\program\soffice.exe`

### Headless mode issues on Linux servers

If running on a headless Linux server (no display), LibreOffice should work out of the box with `--headless`. If you encounter display-related errors, ensure no `DISPLAY` environment variable is set, or install `xvfb` as a fallback:

```bash
sudo apt install xvfb
```

### Concurrent conversion issues

LibreOffice uses a user profile lock that can cause issues with simultaneous conversions. The DocumentConverter mitigates this by using UUID-named temp files, but if you experience locking errors under heavy load, consider running multiple LibreOffice instances with separate user profiles via the `-env:UserInstallation` flag.
