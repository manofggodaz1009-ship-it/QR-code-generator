const http = require('http');
const fs = require('fs');
const path = require('path');

const PORT = 3000;
const BASE_DIR = __dirname;

const server = http.createServer((req, res) => {
    const url = req.url.split('?')[0];

    if (url === '/CNV_QR_UPI_Project.zip') {
        const filePath = path.join(BASE_DIR, 'CNV_QR_UPI_Project.zip');
        if (fs.existsSync(filePath)) {
            const stat = fs.statSync(filePath);
            res.writeHead(200, {
                'Content-Type': 'application/zip',
                'Content-Length': stat.size,
                'Content-Disposition': 'attachment; filename="CNV_QR_UPI_Project.zip"'
            });
            fs.createReadStream(filePath).pipe(res);
            return;
        }
    }

    if (url === '/CNV_QR_UPI_APK.zip') {
        const filePath = path.join(BASE_DIR, 'CNV_QR_UPI_APK.zip');
        if (fs.existsSync(filePath)) {
            const stat = fs.statSync(filePath);
            res.writeHead(200, {
                'Content-Type': 'application/zip',
                'Content-Length': stat.size,
                'Content-Disposition': 'attachment; filename="CNV_QR_UPI_APK.zip"'
            });
            fs.createReadStream(filePath).pipe(res);
            return;
        }
    }

    if (url === '/CNV_QR_UPI.apk') {
        const filePath = path.join(BASE_DIR, 'CNV_QR_UPI.apk');
        if (fs.existsSync(filePath)) {
            const stat = fs.statSync(filePath);
            res.writeHead(200, {
                'Content-Type': 'application/vnd.android.package-archive',
                'Content-Length': stat.size,
                'Content-Disposition': 'attachment; filename="CNV_QR_UPI.apk"'
            });
            fs.createReadStream(filePath).pipe(res);
            return;
        }
    }

    // Default HTML Download Portal
    const html = `<!DOCTYPE html>
<html lang="ta">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>CNV QR & UPI — பதிவிறக்கம் (Download Portal)</title>
    <style>
        * { box-sizing: border-box; margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Noto Sans Tamil", sans-serif; }
        body {
            background-color: #0c0c0f;
            color: #f6f6f9;
            display: flex;
            justify-content: center;
            align-items: center;
            min-height: 100vh;
            padding: 24px 16px;
        }
        .container {
            max-width: 580px;
            width: 100%;
            background: #141419;
            border: 1.5px solid #d4af37;
            border-radius: 24px;
            padding: 36px 26px;
            text-align: center;
            box-shadow: 0 16px 50px rgba(212, 175, 55, 0.2);
        }
        .badge {
            display: inline-block;
            background: rgba(212, 175, 55, 0.15);
            color: #d4af37;
            border: 1px solid rgba(212, 175, 55, 0.4);
            padding: 6px 16px;
            border-radius: 20px;
            font-size: 13px;
            font-weight: 700;
            margin-bottom: 16px;
            letter-spacing: 0.5px;
        }
        h1 {
            color: #f3d068;
            font-size: 28px;
            font-weight: 800;
            margin-bottom: 8px;
        }
        p.subtitle {
            color: #b0b0bc;
            font-size: 14px;
            margin-bottom: 28px;
            line-height: 1.6;
        }
        .btn-group {
            display: flex;
            flex-direction: column;
            gap: 14px;
            margin-bottom: 24px;
        }
        .btn-project {
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 10px;
            background: linear-gradient(135deg, #d4af37, #f3d068);
            color: #0c0c0f;
            text-decoration: none;
            font-size: 16px;
            font-weight: 700;
            padding: 16px 24px;
            border-radius: 14px;
            transition: transform 0.2s, box-shadow 0.2s;
            box-shadow: 0 6px 20px rgba(212, 175, 55, 0.35);
        }
        .btn-zip {
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 10px;
            background: rgba(212, 175, 55, 0.15);
            color: #f3d068;
            border: 1.5px solid #d4af37;
            text-decoration: none;
            font-size: 15px;
            font-weight: 700;
            padding: 14px 24px;
            border-radius: 14px;
            transition: background 0.2s;
        }
        .btn-apk {
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 10px;
            background: transparent;
            color: #cfcfe0;
            border: 1px solid #4a4a58;
            text-decoration: none;
            font-size: 14px;
            font-weight: 600;
            padding: 12px 20px;
            border-radius: 12px;
            transition: border-color 0.2s;
        }
        .btn-project:hover, .btn-zip:hover, .btn-apk:hover {
            transform: translateY(-2px);
        }
        .info-card {
            background: #1b1b22;
            border-radius: 14px;
            padding: 18px;
            text-align: left;
            font-size: 13px;
            color: #cfcfe0;
            line-height: 1.6;
            border-left: 4px solid #d4af37;
        }
        .info-card strong {
            color: #f3d068;
            display: block;
            margin-bottom: 8px;
            font-size: 14px;
        }
        .footer {
            margin-top: 24px;
            font-size: 12px;
            color: #7a7a88;
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="badge">CNV MINISTRIES • OFFICIAL DOWNLOAD</div>
        <h1>CNV QR & UPI</h1>
        <p class="subtitle">முழுமையான Android Project மற்றும் APK தயாராக உள்ளது. தேவையான கோப்பைக் கீழே கிளிக் செய்து நேரடியாகப் பதிவிறக்கவும்.</p>
        
        <div class="btn-group">
            <a href="/CNV_QR_UPI_Project.zip" class="btn-project" download="CNV_QR_UPI_Project.zip">
                📦 முழு Project ZIP பதிவிறக்கு (Android Studio)
            </a>
            <a href="/CNV_QR_UPI_APK.zip" class="btn-zip" download="CNV_QR_UPI_APK.zip">
                📥 CNV_QR_UPI_APK.zip பதிவிறக்கு (25 MB)
            </a>
            <a href="/CNV_QR_UPI.apk" class="btn-apk" download="CNV_QR_UPI.apk">
                ⚡ நேரடி CNV_QR_UPI.apk பதிவிறக்கு (26 MB)
            </a>
        </div>

        <div class="info-card">
            <strong>📋 பயன்படுத்துவது எப்படி?</strong>
            • <b>Android Studio Project:</b> Project ZIP-ஐ Extract செய்து Android Studio-வில் 'Open' கொடுத்து இயக்கலாம்.<br>
            • <b>மொபைலில் APK நிறுவல்:</b> APK-ஐ உங்கள் Android மொபைலில் நேரடியாகத் தொட்டு "Install" செய்யலாம்.
        </div>

        <div class="footer">
            Package: com.cnvministries.qrupi • Version 1.0 • 100% Native Kotlin & Jetpack Compose
        </div>
    </div>
</body>
</html>`;

    res.writeHead(200, { 'Content-Type': 'text/html; charset=utf-8' });
    res.end(html);
});

server.listen(PORT, '0.0.0.0', () => {
    console.log(`Server listening on port ${PORT}`);
});
