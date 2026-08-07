# Mizo MixStation AI V7.4.4 — Clean Build

Fresh Android Java project built to avoid the previous duplicated/corrupted `GeminiAiService.java` and uninitialized Text-to-Speech errors.

## Before running

1. Firebase Console → Project settings → Android app `com.mizomixstation.app`.
2. Download the **Android** `google-services.json`.
3. Copy it to: `app/google-services.json`.
4. Firebase Console → AI services → AI Logic → Get started → choose Gemini Developer API.
5. During testing, configure the App Check debug token when Firebase asks for it.

The included `google-services.json.example` is only a guide. Do not rename the web Firebase config to `google-services.json`.

## GitHub Actions

For a real Firebase-connected APK, create repository secret:

- Name: `GOOGLE_SERVICES_JSON_BASE64`
- Value: Base64 text of your real Android `google-services.json`.

In Termux:

```bash
base64 -w 0 app/google-services.json
```

Copy the output into the GitHub secret. Then push to `main` or run the workflow manually.

If the secret is missing, GitHub Actions uses a build-only placeholder so compilation can still be tested; Firebase features will not work in that placeholder APK.

## Termux upload

```bash
cd /storage/emulated/0/Download/MizoMixStation_AI_V7_4_4_CLEAN
git init
git config --global --add safe.directory "$PWD"
git add .
git commit -m "Mizo MixStation AI V7.4.4 clean build"
git branch -M main
git remote add origin https://github.com/mizomixstation/mizo-mixstation-android.git
git push -u origin main --force
```

Use GitHub username `mizomixstation`; use your Personal Access Token as the password.
