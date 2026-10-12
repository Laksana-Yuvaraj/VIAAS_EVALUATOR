package com.example.ui.components

enum class AppLanguage(val code: String, val label: String) {
    ENGLISH("en", "English"),
    TAMIL("ta", "தமிழ் (Tamil)")
}

object LanguageManager {
    fun get(key: String, lang: AppLanguage): String {
        return when (lang) {
            AppLanguage.TAMIL -> tamilStrings[key] ?: englishStrings[key] ?: key
            AppLanguage.ENGLISH -> englishStrings[key] ?: key
        }
    }

    private val englishStrings = mapOf(
        "app_title" to "VIAAS Evaluator Pro",
        "tagline" to "Smart Assessment & Google Sheets Keyword Answer Grading System",
        "portal_select" to "Select Portal",
        "educator_portal" to "Educator Portal",
        "student_portal" to "Student Portal",
        "new_exam" to "New Assessment",
        "review_edit" to "Review & Edit",
        "begin_test" to "Begin Assessment Session",
        "view_history" to "View Exam History & Past Results",
        "question" to "Question",
        "of" to "of",
        "marks" to "Marks",
        "previous" to "Previous",
        "next" to "Next",
        "finish_submit" to "Finish & Submit",
        "time_spent" to "Time Spent on Question",
        "matched_keywords" to "Matched Core Keywords",
        "missing_keywords" to "Missing Required Keywords",
        "ai_feedback" to "Gemini AI Grading Feedback",
        "download_report" to "Download Report (CSV)",
        "share" to "Share",
        "section_transition" to "Proceed to Next Section",
        "passcode_required" to "Enter Master Exam Passcode",
        "draft_saved" to "Draft Progress Auto-Saved",
        "certificate_title" to "CERTIFICATE OF ASSESSMENT COMPLETION"
    )

    private val tamilStrings = mapOf(
        "app_title" to "வியாஸ் மதிப்பீட்டாளர் புரோ",
        "tagline" to "நுண்ணறிவு மதிப்பீடு மற்றும் கூகிள் தாள்கள் முக்கிய சொல் மதிப்பீட்டு முறைமை",
        "portal_select" to "நுழைவு வாயிலைத் தேர்ந்தெடுக்கவும்",
        "educator_portal" to "ஆசிரியர் போர்டல்",
        "student_portal" to "மாணவர் போர்டல்",
        "new_exam" to "புதிய தேர்வு உருவாக்கவும்",
        "review_edit" to "மதிப்பாய்வு செய்து திருத்துக",
        "begin_test" to "தேர்வைத் தொடங்கவும்",
        "view_history" to "எனது முந்தைய தேர்வு முடிவுகள்",
        "question" to "கேள்வி",
        "of" to "இல்",
        "marks" to "மதிப்பெண்கள்",
        "previous" to "முந்தையது",
        "next" to "அடுத்தது",
        "finish_submit" to "முடித்து சமர்ப்பிக்கவும்",
        "time_spent" to "கேள்விக்கு செலவழித்த நேரம்",
        "matched_keywords" to "பொருந்திய முக்கிய சொற்கள்",
        "missing_keywords" to "விடுபட்ட முக்கிய சொற்கள்",
        "ai_feedback" to "ஜெமினி செயற்கை நுண்ணறிவு பின்னூட்டம்",
        "download_report" to "அறிக்கையைப் பதிவிறக்குக (CSV)",
        "share" to "பகிரவும்",
        "section_transition" to "அடுத்த பகுதிக்குச் செல்லவும்",
        "passcode_required" to "தேர்வு கடவுச்சொல்லை உள்ளிடவும்",
        "draft_saved" to "வரைவு தானாக சேமிக்கப்பட்டது",
        "certificate_title" to "தேர்வு நிறைவு சான்றிதழ்"
    )
}
