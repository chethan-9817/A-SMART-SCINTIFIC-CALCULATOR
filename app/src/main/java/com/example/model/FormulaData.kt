package com.example.model

data class FormulaItem(
    val name: String,
    val latexOrFormula: String,
    val description: String,
    val category: String,
    val sampleInput: String = ""
)

object FormulaLibrary {
    val categories = listOf("All", "Calculus", "Trigonometry", "Algebra", "Physics", "Constants")

    val allFormulas = listOf(
        // Calculus
        FormulaItem(
            name = "Power Rule Derivative",
            latexOrFormula = "d/dx [x^n] = n · x^(n-1)",
            description = "Fundamental rule for taking derivatives of polynomial terms",
            category = "Calculus",
            sampleInput = "3*x^2"
        ),
        FormulaItem(
            name = "Product Rule",
            latexOrFormula = "(u · v)' = u'·v + u·v'",
            description = "Derivative of the product of two functions",
            category = "Calculus"
        ),
        FormulaItem(
            name = "Quotient Rule",
            latexOrFormula = "(u / v)' = (u'·v - u·v') / v^2",
            description = "Derivative of the quotient of two functions",
            category = "Calculus"
        ),
        FormulaItem(
            name = "Chain Rule",
            latexOrFormula = "d/dx [f(g(x))] = f'(g(x)) · g'(x)",
            description = "Derivative of composite functions",
            category = "Calculus"
        ),
        FormulaItem(
            name = "Integration by Parts",
            latexOrFormula = "∫ u dv = u·v - ∫ v du",
            description = "Formula for integrating products of functions",
            category = "Calculus"
        ),
        FormulaItem(
            name = "Fundamental Theorem of Calculus",
            latexOrFormula = "∫[a, b] f(x) dx = F(b) - F(a)",
            description = "Relates differentiation with integration",
            category = "Calculus"
        ),
        FormulaItem(
            name = "Derivative of sin(x)",
            latexOrFormula = "d/dx [sin(x)] = cos(x)",
            description = "Trigonometric differentiation",
            category = "Calculus"
        ),
        FormulaItem(
            name = "Derivative of ln(x)",
            latexOrFormula = "d/dx [ln(x)] = 1 / x",
            description = "Logarithmic derivative",
            category = "Calculus"
        ),

        // Trigonometry
        FormulaItem(
            name = "Pythagorean Identity",
            latexOrFormula = "sin²(θ) + cos²(θ) = 1",
            description = "Fundamental identity in Euclidean trigonometry",
            category = "Trigonometry"
        ),
        FormulaItem(
            name = "Double Angle for Sine",
            latexOrFormula = "sin(2θ) = 2 · sin(θ) · cos(θ)",
            description = "Sine of double angle identity",
            category = "Trigonometry"
        ),
        FormulaItem(
            name = "Double Angle for Cosine",
            latexOrFormula = "cos(2θ) = cos²(θ) - sin²(θ)",
            description = "Cosine double angle identity",
            category = "Trigonometry"
        ),
        FormulaItem(
            name = "Law of Sines",
            latexOrFormula = "a / sin(A) = b / sin(B) = c / sin(C)",
            description = "Relates side lengths of triangles to angles",
            category = "Trigonometry"
        ),
        FormulaItem(
            name = "Law of Cosines",
            latexOrFormula = "c² = a² + b² - 2ab · cos(C)",
            description = "Generalization of Pythagorean theorem for any triangle",
            category = "Trigonometry"
        ),
        FormulaItem(
            name = "Tangent Definition",
            latexOrFormula = "tan(θ) = sin(θ) / cos(θ)",
            description = "Ratio of sine over cosine",
            category = "Trigonometry"
        ),

        // Algebra
        FormulaItem(
            name = "Quadratic Formula",
            latexOrFormula = "x = (-b ± √(b² - 4ac)) / (2a)",
            description = "Roots of polynomial ax² + bx + c = 0",
            category = "Algebra",
            sampleInput = "(-4 + sqrt(4^2 - 4*1*3)) / (2*1)"
        ),
        FormulaItem(
            name = "Logarithm Product Rule",
            latexOrFormula = "log_b(xy) = log_b(x) + log_b(y)",
            description = "Logarithm of product turns into sum of logs",
            category = "Algebra"
        ),
        FormulaItem(
            name = "Logarithm Power Rule",
            latexOrFormula = "log_b(x^k) = k · log_b(x)",
            description = "Exponent moves to multiplication factor",
            category = "Algebra"
        ),
        FormulaItem(
            name = "Change of Base Rule",
            latexOrFormula = "log_b(x) = ln(x) / ln(b)",
            description = "Compute any base logarithm using natural log",
            category = "Algebra"
        ),
        FormulaItem(
            name = "Difference of Squares",
            latexOrFormula = "a² - b² = (a - b)(a + b)",
            description = "Algebraic polynomial factorization",
            category = "Algebra"
        ),

        // Physics
        FormulaItem(
            name = "Kinematics Position",
            latexOrFormula = "x = x₀ + v₀·t + (1/2)·a·t²",
            description = "Uniform acceleration displacement equation",
            category = "Physics"
        ),
        FormulaItem(
            name = "Kinematics Velocity",
            latexOrFormula = "v² = v₀² + 2·a·Δx",
            description = "Velocity squared without explicit time",
            category = "Physics"
        ),
        FormulaItem(
            name = "Newton's Second Law",
            latexOrFormula = "F = m · a",
            description = "Net force equals mass times acceleration",
            category = "Physics"
        ),
        FormulaItem(
            name = "Kinetic Energy",
            latexOrFormula = "KE = (1/2) · m · v²",
            description = "Mechanical kinetic energy of moving body",
            category = "Physics"
        ),
        FormulaItem(
            name = "Ohm's Law",
            latexOrFormula = "V = I · R",
            description = "Voltage equals current times resistance",
            category = "Physics"
        ),
        FormulaItem(
            name = "Ideal Gas Law",
            latexOrFormula = "P · V = n · R · T",
            description = "Equation of state of a hypothetical ideal gas",
            category = "Physics"
        ),

        // Physical Constants
        FormulaItem(
            name = "Speed of Light (c)",
            latexOrFormula = "299,792,458 m/s",
            description = "Speed of light in vacuum",
            category = "Constants",
            sampleInput = "299792458"
        ),
        FormulaItem(
            name = "Planck Constant (h)",
            latexOrFormula = "6.62607015 × 10⁻³⁴ J·s",
            description = "Fundamental quantum physics action constant",
            category = "Constants",
            sampleInput = "6.62607015E-34"
        ),
        FormulaItem(
            name = "Gravitational Constant (G)",
            latexOrFormula = "6.67430 × 10⁻¹¹ m³/(kg·s²)",
            description = "Newtonian gravitational constant",
            category = "Constants",
            sampleInput = "6.6743E-11"
        ),
        FormulaItem(
            name = "Standard Gravity (g)",
            latexOrFormula = "9.80665 m/s²",
            description = "Nominal acceleration of Earth gravity at sea level",
            category = "Constants",
            sampleInput = "9.80665"
        ),
        FormulaItem(
            name = "Euler's Number (e)",
            latexOrFormula = "2.718281828459...",
            description = "Base of natural logarithm",
            category = "Constants",
            sampleInput = "2.718281828"
        ),
        FormulaItem(
            name = "Pi (π)",
            latexOrFormula = "3.14159265358979...",
            description = "Ratio of circle circumference to diameter",
            category = "Constants",
            sampleInput = "3.1415926535"
        )
    )
}
