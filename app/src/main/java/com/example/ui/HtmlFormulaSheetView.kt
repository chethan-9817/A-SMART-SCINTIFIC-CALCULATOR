package com.example.ui

import android.annotation.SuppressLint
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun HtmlFormulaSheetView(
    onDismiss: () -> Unit,
    onFormulaSelected: (String) -> Unit = {}
) {
    var selectedCategory by remember { mutableStateOf("Calculus") }
    val clipboardManager = LocalClipboardManager.current
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }

    val categories = listOf("Calculus", "Trigonometry", "Algebra", "Physics", "Chemistry")

    val htmlContent = remember(selectedCategory) {
        generateHtmlCheatSheet(selectedCategory)
    }

    Surface(
        modifier = Modifier
            .fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "HTML Math & Formula Vault",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Interactive HTML5/CSS Math Engine & Cheat Reference",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close HTML Sheet",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Category Chips Row
            ScrollableTabRow(
                selectedTabIndex = categories.indexOf(selectedCategory).coerceAtLeast(0),
                edgePadding = 16.dp,
                divider = {}
            ) {
                categories.forEach { category ->
                    Tab(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        text = {
                            Text(
                                text = category,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Embedded HTML5 WebView
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        WebView(ctx).apply {
                            webViewClient = WebViewClient()
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            settings.loadWithOverviewMode = true
                            settings.useWideViewPort = true
                            settings.cacheMode = WebSettings.LOAD_NO_CACHE
                            setBackgroundColor(0xFF0F172A.toInt())
                            loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
                            webViewInstance = this
                        }
                    },
                    update = { view ->
                        view.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
                    }
                )
            }

            // Bottom action bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .navigationBarsPadding(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        webViewInstance?.reload()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Reload HTML", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Refresh")
                }

                FilledTonalButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(getCategoryFormulasText(selectedCategory)))
                    }
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy Formulas", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copy All $selectedCategory")
                }
            }
        }
    }
}

private fun getCategoryFormulasText(cat: String): String {
    return when (cat) {
        "Calculus" -> "d/dx[x^n] = n*x^(n-1)\n(u*v)' = u'v + uv'\n(u/v)' = (u'v - uv')/v^2\n∫ x^n dx = x^(n+1)/(n+1) + C\n∫ 1/x dx = ln|x| + C\n∫ e^x dx = e^x + C\n∫ sin(x) dx = -cos(x) + C"
        "Trigonometry" -> "sin^2(x) + cos^2(x) = 1\n1 + tan^2(x) = sec^2(x)\nsin(2x) = 2sin(x)cos(x)\ncos(2x) = cos^2(x) - sin^2(x)\ntan(2x) = 2tan(x)/(1 - tan^2(x))"
        "Algebra" -> "x = [-b ± sqrt(b^2 - 4ac)] / (2a)\nlog(xy) = log(x) + log(y)\nlog(x/y) = log(x) - log(y)\nlog(x^k) = k*log(x)\na^2 - b^2 = (a - b)(a + b)"
        "Physics" -> "x = x0 + v0*t + 0.5*a*t^2\nv^2 = v0^2 + 2*a*dx\nF = m*a\nKE = 0.5*m*v^2\nPE = m*g*h\nV = I*R\nP = V*I"
        else -> "PV = nRT\npH = -log[H+]\nE = mc^2\nlambda = h / p"
    }
}

private fun generateHtmlCheatSheet(category: String): String {
    val content = when (category) {
        "Calculus" -> """
            <div class="card">
                <h2>📈 Derivatives Fast Lookup</h2>
                <div class="formula-row"><span class="name">Power Rule</span><span class="eq">\(\frac{d}{dx}[x^n] = n x^{n-1}\)</span></div>
                <div class="formula-row"><span class="name">Product Rule</span><span class="eq">\((u \cdot v)' = u'v + uv'\)</span></div>
                <div class="formula-row"><span class="name">Quotient Rule</span><span class="eq">\(\left(\frac{u}{v}\right)' = \frac{u'v - uv'}{v^2}\)</span></div>
                <div class="formula-row"><span class="name">Chain Rule</span><span class="eq">\(\frac{d}{dx}[f(g(x))] = f'(g(x)) g'(x)\)</span></div>
                <div class="formula-row"><span class="name">Exponential</span><span class="eq">\(\frac{d}{dx}[e^x] = e^x, \quad \frac{d}{dx}[a^x] = a^x \ln(a)\)</span></div>
                <div class="formula-row"><span class="name">Natural Log</span><span class="eq">\(\frac{d}{dx}[\ln(x)] = \frac{1}{x}\)</span></div>
                <div class="formula-row"><span class="name">Sine / Cosine</span><span class="eq">\(\sin' = \cos(x), \quad \cos' = -\sin(x)\)</span></div>
                <div class="formula-row"><span class="name">Tangent</span><span class="eq">\(\tan'(x) = \sec^2(x)\)</span></div>
            </div>

            <div class="card">
                <h2>∫ Standard Integrals</h2>
                <div class="formula-row"><span class="name">Power Integral</span><span class="eq">\(\int x^n dx = \frac{x^{n+1}}{n+1} + C \quad (n \neq -1)\)</span></div>
                <div class="formula-row"><span class="name">Reciprocal</span><span class="eq">\(\int \frac{1}{x} dx = \ln|x| + C\)</span></div>
                <div class="formula-row"><span class="name">Exponential</span><span class="eq">\(\int e^{kx} dx = \frac{1}{k} e^{kx} + C\)</span></div>
                <div class="formula-row"><span class="name">By Parts</span><span class="eq">\(\int u \, dv = uv - \int v \, du\)</span></div>
                <div class="formula-row"><span class="name">Trig Integrals</span><span class="eq">\(\int \sin(x)dx = -\cos(x), \quad \int \cos(x)dx = \sin(x)\)</span></div>
            </div>
        """.trimIndent()

        "Trigonometry" -> """
            <div class="card">
                <h2>📐 Fundamental Identities</h2>
                <div class="formula-row"><span class="name">Pythagorean</span><span class="eq">\(\sin^2(\theta) + \cos^2(\theta) = 1\)</span></div>
                <div class="formula-row"><span class="name">Secant</span><span class="eq">\(1 + \tan^2(\theta) = \sec^2(\theta)\)</span></div>
                <div class="formula-row"><span class="name">Cosecant</span><span class="eq">\(1 + \cot^2(\theta) = \csc^2(\theta)\)</span></div>
            </div>

            <div class="card">
                <h2>⚡ Double & Half Angle Rules</h2>
                <div class="formula-row"><span class="name">Double Sine</span><span class="eq">\(\sin(2\theta) = 2\sin(\theta)\cos(\theta)\)</span></div>
                <div class="formula-row"><span class="name">Double Cosine</span><span class="eq">\(\cos(2\theta) = \cos^2(\theta) - \sin^2(\theta) = 2\cos^2 - 1\)</span></div>
                <div class="formula-row"><span class="name">Double Tangent</span><span class="eq">\(\tan(2\theta) = \frac{2\tan(\theta)}{1 - \tan^2(\theta)}\)</span></div>
                <div class="formula-row"><span class="name">Half Angle Sine</span><span class="eq">\(\sin\left(\frac{\theta}{2}\right) = \pm\sqrt{\frac{1 - \cos(\theta)}{2}}\)</span></div>
                <div class="formula-row"><span class="name">Half Angle Cos</span><span class="eq">\(\cos\left(\frac{\theta}{2}\right) = \pm\sqrt{\frac{1 + \cos(\theta)}{2}}\)</span></div>
            </div>

            <div class="card">
                <h2>🔺 Triangles & Geometry</h2>
                <div class="formula-row"><span class="name">Law of Sines</span><span class="eq">\(\frac{a}{\sin A} = \frac{b}{\sin B} = \frac{c}{\sin C}\)</span></div>
                <div class="formula-row"><span class="name">Law of Cosines</span><span class="eq">\(c^2 = a^2 + b^2 - 2ab\cos(C)\)</span></div>
            </div>
        """.trimIndent()

        "Algebra" -> """
            <div class="card">
                <h2>🧮 Polynomials & Quadratics</h2>
                <div class="formula-row"><span class="name">Quadratic Formula</span><span class="eq">\(x = \frac{-b \pm \sqrt{b^2 - 4ac}}{2a}\)</span></div>
                <div class="formula-row"><span class="name">Discriminant</span><span class="eq">\(\Delta = b^2 - 4ac \quad (>0: 2\text{ real}, =0: 1, <0: \text{complex})\)</span></div>
                <div class="formula-row"><span class="name">Diff of Squares</span><span class="eq">\(a^2 - b^2 = (a - b)(a + b)\)</span></div>
                <div class="formula-row"><span class="name">Diff of Cubes</span><span class="eq">\(a^3 - b^3 = (a - b)(a^2 + ab + b^2)\)</span></div>
            </div>

            <div class="card">
                <h2>📊 Logarithms & Exponents</h2>
                <div class="formula-row"><span class="name">Product</span><span class="eq">\(\log(xy) = \log(x) + \log(y)\)</span></div>
                <div class="formula-row"><span class="name">Quotient</span><span class="eq">\(\log(x/y) = \log(x) - \log(y)\)</span></div>
                <div class="formula-row"><span class="name">Power</span><span class="eq">\(\log(x^k) = k \cdot \log(x)\)</span></div>
                <div class="formula-row"><span class="name">Change of Base</span><span class="eq">\(\log_b(a) = \frac{\ln(a)}{\ln(b)}\)</span></div>
            </div>
        """.trimIndent()

        "Physics" -> """
            <div class="card">
                <h2>🚀 Kinematics & Dynamics</h2>
                <div class="formula-row"><span class="name">Velocity</span><span class="eq">\(v = v_0 + at\)</span></div>
                <div class="formula-row"><span class="name">Displacement</span><span class="eq">\(x = x_0 + v_0 t + \frac{1}{2}at^2\)</span></div>
                <div class="formula-row"><span class="name">Timeless</span><span class="eq">\(v^2 = v_0^2 + 2a\Delta x\)</span></div>
                <div class="formula-row"><span class="name">Newton's 2nd</span><span class="eq">\(F = ma\)</span></div>
                <div class="formula-row"><span class="name">Kinetic Energy</span><span class="eq">\(KE = \frac{1}{2}mv^2\)</span></div>
                <div class="formula-row"><span class="name">Work</span><span class="eq">\(W = F \cdot d \cos(\theta)\)</span></div>
            </div>

            <div class="card">
                <h2>⚡ Electricity & Magnetism</h2>
                <div class="formula-row"><span class="name">Ohm's Law</span><span class="eq">\(V = I \cdot R\)</span></div>
                <div class="formula-row"><span class="name">Electric Power</span><span class="eq">\(P = VI = I^2 R = \frac{V^2}{R}\)</span></div>
                <div class="formula-row"><span class="name">Coulomb's Law</span><span class="eq">\(F = k_e \frac{|q_1 q_2|}{r^2}\)</span></div>
            </div>
        """.trimIndent()

        else -> """
            <div class="card">
                <h2>🧪 Chemistry & Gas Laws</h2>
                <div class="formula-row"><span class="name">Ideal Gas Law</span><span class="eq">\(PV = nRT\)</span></div>
                <div class="formula-row"><span class="name">Molarity</span><span class="eq">\(M = \frac{\text{moles of solute}}{\text{liters of solution}}\)</span></div>
                <div class="formula-row"><span class="name">pH Definition</span><span class="eq">\(\text{pH} = -\log[H^+]\)</span></div>
                <div class="formula-row"><span class="name">Dilution Law</span><span class="eq">\(M_1 V_1 = M_2 V_2\)</span></div>
            </div>

            <div class="card">
                <h2>🌐 Quantum & Thermodynamics</h2>
                <div class="formula-row"><span class="name">Mass-Energy</span><span class="eq">\(E = mc^2\)</span></div>
                <div class="formula-row"><span class="name">Photon Energy</span><span class="eq">\(E = h\nu = \frac{hc}{\lambda}\)</span></div>
                <div class="formula-row"><span class="name">Gibbs Free Energy</span><span class="eq">\(\Delta G = \Delta H - T\Delta S\)</span></div>
            </div>
        """.trimIndent()
    }

    return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
            <title>$category Cheat Sheet</title>
            <style>
                * { box-sizing: border-box; margin: 0; padding: 0; }
                body {
                    background-color: #0b1120;
                    color: #f1f5f9;
                    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
                    padding: 14px;
                    line-height: 1.5;
                }
                .badge {
                    display: inline-block;
                    background: #1e293b;
                    color: #38bdf8;
                    padding: 4px 10px;
                    border-radius: 9999px;
                    font-size: 11px;
                    font-weight: 700;
                    margin-bottom: 12px;
                    text-transform: uppercase;
                    letter-spacing: 0.5px;
                    border: 1px solid #334155;
                }
                .card {
                    background: #131d33;
                    border: 1px solid #1e293b;
                    border-radius: 12px;
                    padding: 14px;
                    margin-bottom: 14px;
                    box-shadow: 0 4px 12px rgba(0,0,0,0.3);
                }
                h2 {
                    font-size: 15px;
                    color: #38bdf8;
                    font-weight: 600;
                    margin-bottom: 10px;
                    padding-bottom: 6px;
                    border-bottom: 1px solid #1e293b;
                }
                .formula-row {
                    display: flex;
                    justify-content: space-between;
                    align-items: center;
                    padding: 8px 6px;
                    border-radius: 6px;
                    margin-bottom: 4px;
                    background: rgba(30, 41, 59, 0.4);
                }
                .formula-row:nth-child(even) {
                    background: rgba(30, 41, 59, 0.7);
                }
                .name {
                    font-size: 12px;
                    color: #94a3b8;
                    font-weight: 500;
                    flex: 1;
                }
                .eq {
                    font-size: 13px;
                    color: #f8fafc;
                    font-family: "Courier New", Courier, monospace;
                    font-weight: 600;
                    background: #0f172a;
                    padding: 3px 8px;
                    border-radius: 4px;
                    border: 1px solid #334155;
                    text-align: right;
                }
                .note {
                    font-size: 11px;
                    color: #64748b;
                    margin-top: 12px;
                    text-align: center;
                }
            </style>
            <!-- MathJax for rendering rich TeX formulas in HTML -->
            <script src="https://polyfill.io/v3/polyfill.min.js?features=es6"></script>
            <script id="MathJax-script" async src="https://cdn.jsdelivr.net/npm/mathjax@3/es5/tex-mml-chtml.js"></script>
        </head>
        <body>
            <span class="badge">OmniCalc AI • HTML Engine</span>
            $content
            <p class="note">High-Precision Verified Academic Formulas • OmniCalc</p>
        </body>
        </html>
    """.trimIndent()
}
