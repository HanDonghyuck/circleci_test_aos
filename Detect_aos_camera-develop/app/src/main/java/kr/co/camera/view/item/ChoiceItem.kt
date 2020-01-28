package kr.co.camera.view.item

data class ChoiceItem(
    var title: String
)

fun getChoiceData(): ArrayList<ChoiceItem> {
    val data: ArrayList<ChoiceItem> = ArrayList()

    data.add(ChoiceItem("1. 코 사각형 O / 눈 O / 콧구멍 O"))
    data.add(ChoiceItem("2. 코 사각형 O / 눈 O / 콧구멍 X"))
    data.add(ChoiceItem("3. 테두리 사각형 O / 눈 O / 콧구멍 O"))
    data.add(ChoiceItem("4. 테두리 사각형 O / 눈 O / 콧구멍 X"))

    return data
}

fun getChoiceHead(): ArrayList<ChoiceItem> {
    val data: ArrayList<ChoiceItem> = ArrayList()

    data.add(ChoiceItem("1. 장두종"))
    data.add(ChoiceItem("2. 단두종"))

    return data
}