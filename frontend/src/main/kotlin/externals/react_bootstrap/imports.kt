//package externals.react_bootstrap
//
//import kotlinx.html.ButtonFormEncType
//import kotlinx.html.ButtonFormMethod
//import kotlinx.html.ButtonType
//import kotlinx.html.CommandType
//import kotlinx.html.Dir
//import kotlinx.html.HTMLTag
//import kotlinx.html.HtmlBlockTag
//import kotlinx.html.InputFormMethod
//import kotlinx.html.TagConsumer
//import kotlinx.html.attributes.Attribute
//import kotlinx.html.attributes.BooleanAttribute
//import kotlinx.html.attributes.EnumAttribute
//import kotlinx.html.attributes.StringAttribute
//import kotlinx.html.attributes.StringSetAttribute
//import kotlinx.html.attributes.TickerAttribute
//import kotlinx.html.attributesMapOf
//import react.RBuilder
//import react.ReactElement
//import react.createElement
//import react.dom.RDOMBuilder
//import react.dom.createPortal
//import react.dom.tag
//
//internal val inputFormMethodValues: Map<String, InputFormMethod> = InputFormMethod.values().associateBy { it.realValue }
//internal val buttonFormEncTypeValues: Map<String, ButtonFormEncType> =
//    ButtonFormEncType.values().associateBy { it.realValue }
//internal val buttonFormMethodValues: Map<String, ButtonFormMethod> =
//    ButtonFormMethod.values().associateBy { it.realValue }
//internal val buttonTypeValues: Map<String, ButtonType> = ButtonType.values().associateBy { it.realValue }
//internal val commandTypeValues: Map<String, CommandType> = CommandType.values().associateBy { it.realValue }
//internal val dirValues: Map<String, Dir> = Dir.values().associateBy { it.realValue }
//
//internal val attributeString: Attribute<String> = StringAttribute()
//internal val attributeBoolean: Attribute<Boolean> = BooleanAttribute()
//internal val attributeStringString: Attribute<String> = StringAttribute()
//
//internal val attributeSetStringStringSet: Attribute<Set<String>> = StringSetAttribute()
//
//internal val attributeBooleanBoolean: Attribute<Boolean> = BooleanAttribute()
//
//internal val attributeBooleanBooleanOnOff: Attribute<Boolean> = BooleanAttribute("on", "off")
//
//internal val attributeBooleanTicker: Attribute<Boolean> = TickerAttribute()
//
//internal val attributeButtonFormEncTypeEnumButtonFormEncTypeValues: Attribute<ButtonFormEncType> = EnumAttribute(
//    buttonFormEncTypeValues
//)
//
//internal val attributeButtonFormMethodEnumButtonFormMethodValues: Attribute<ButtonFormMethod> = EnumAttribute(
//    buttonFormMethodValues
//)
//
//internal val attributeButtonTypeEnumButtonTypeValues: Attribute<ButtonType> = EnumAttribute(buttonTypeValues)
//
//internal val attributeCommandTypeEnumCommandTypeValues: Attribute<CommandType> = EnumAttribute(commandTypeValues)
//
////internal val attributeDirEnumDirValues : Attribute<Dir> = EnumAttribute(dirValues)
////
////internal val attributeDraggableEnumDraggableValues : Attribute<Draggable> = EnumAttribute(draggableValues)
////
////internal val attributeFormEncTypeEnumFormEncTypeValues : Attribute<FormEncType> = EnumAttribute(formEncTypeValues)
////
////internal val attributeFormMethodEnumFormMethodValues : Attribute<FormMethod> = EnumAttribute(formMethodValues)
//
//internal val attributeInputFormMethodEnumInputFormMethodValues: Attribute<InputFormMethod> = EnumAttribute(
//    inputFormMethodValues
//)
////internal val attributeInputTypeEnumInputTypeValues : Attribute<InputType> = EnumAttribute(inputTypeValues)
////
////internal val attributeRunAtEnumRunAtValues : Attribute<RunAt> = EnumAttribute(runAtValues)
////internal val attributeTextAreaWrapEnumTextAreaWrapValues : Attribute<TextAreaWrap> = EnumAttribute(textAreaWrapValues)
////internal val attributeThScopeEnumThScopeValues : Attribute<ThScope> = EnumAttribute(thScopeValues)
//
//@Suppress("unused")
//open class JUMBOTRON(initialAttributes: Map<String, String>, override val consumer: TagConsumer<*>) :
//    HTMLTag("Jumbotron", consumer, initialAttributes, null, false, false),
//    HtmlBlockTag {
//    var fluid: Boolean
//        get() = attributeBoolean.get(this, "fluid")
//        set(newValue) {
//            attributeBoolean.set(this, "fluid", newValue)
//        }
//}
//
//inline fun RBuilder.jumbotron(classes: String? = null, block: RDOMBuilder<JUMBOTRON>.() -> Unit): ReactElement =
//    tag(block) {
//        JUMBOTRON(
//            attributesMapOf("class", classes), it
//        )
//    }
//
//@Suppress("unused")
//open class ALERT(initialAttributes: Map<String, String>, override val consumer: TagConsumer<*>) :
//    HTMLTag("Alert", consumer, initialAttributes, null, false, false),
//    HtmlBlockTag {
//    var variant: String
//        get() = attributeString.get(this, "variant")
//        set(newValue) {
//            attributeString.set(this, "variant", newValue)
//        }
//    var dismissible: Boolean
//        get() = attributeBoolean.get(this, "dismissible")
//        set(newValue) {
//            attributeBoolean.set(this, "dismissible", newValue)
//        }
//}
//
//inline fun RBuilder.alert(variant: String, dismissible: Boolean, block: RDOMBuilder<ALERT>.() -> Unit): ReactElement = tag(block) {
//    ALERT(
//        attributesMapOf(
//            "variant", variant,
//            "dismissible", dismissible.toString()
//        ), it
//    )
//}
//
//
//
//inline fun RBuilder.test(): ReactElement = createElement()