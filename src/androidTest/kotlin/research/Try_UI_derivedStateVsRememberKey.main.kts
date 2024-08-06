
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.io.File

println()
println("-------------------------------------------------------------------------------------------")
println()

val class_name = "Try_UI_derviedStateVsRememberKey"
val file = class_name + ".kt"
val file_template = class_name + ".kt"
val file_backup = class_name + ".kt.backup"
val time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"))

val composables = mapOf(
    "DerivedState_from_Value"                   to "val value by remember                            { derivedStateOf { externalValue             } }",
    "DerivedState_from_Param"                   to "val value by remember                            { derivedStateOf { param                     } }",
    "RememberKey_Value_stateofValue"            to "val value by remember(externalValue            ) { mutableStateOf ( externalValue             ) }",
    "RememberKey_Value_stateofValue10"          to "val value by remember(externalValue            ) { mutableStateOf ( externalValue        / 10 ) }",
    "RememberKey_Value10_stateofValue10"        to "val value by remember(externalValue        / 10) { mutableStateOf ( externalValue        / 10 ) }",
    "RememberKey_Value_Value"                   to "val value =  remember(externalValue            ) {                  externalValue               }",
    "RememberKey_Value_Value10"                 to "val value =  remember(externalValue            ) {                  externalValue        / 10   }",
    "RememberKey_Value10_Value10"               to "val value =  remember(externalValue        / 10) {                  externalValue        / 10   }",
    "Remember__stateofParam"                    to "val value by remember                            { mutableStateOf ( param                     ) }",
    "Remember__stateofParam10"                  to "val value by remember                            { mutableStateOf ( param                / 10 ) }",
    "Remember__Param"                           to "val value =  remember                            { param                                        }",
    "Remember__Param10"                         to "val value =  remember                            { param                                 / 10   }",
    "DerivedState_State"                        to "val value by remember                            { derivedStateOf { externalState.value       } }",
    "DerivedState_State10"                      to "val value by remember                            { derivedStateOf { externalState.value  / 10 } }",
    "DerivedState_StateValue"                   to "val value by remember                            { derivedStateOf { externalStateValue        } }",
    "DerivedState_StateValue10"                 to "val value by remember                            { derivedStateOf { externalStateValue   / 10 } }",
    "DerivedState_byState"                      to "val value by remember                            { derivedStateOf { externalByState           } }",
    "DerivedState_byState10"                    to "val value by remember                            { derivedStateOf { externalByState      / 10 } }",
    "DerivedState_byStateValue"                 to "val value by remember                            { derivedStateOf { externalByStateValue      } }",
    "DerivedState_byStateValue10"               to "val value by remember                            { derivedStateOf { externalByStateValue / 10 } }",
    "RememberKey_State_stateofState"            to "val value by remember(externalState.value      ) { mutableStateOf ( externalState.value       ) }",
    "RememberKey_State_stateofState10"          to "val value by remember(externalState.value      ) { mutableStateOf ( externalState.value  / 10 ) }",
    "RememberKey_State10_stateofState10"        to "val value by remember(externalState.value  / 10) { mutableStateOf ( externalState.value  / 10 ) }",
    "RememberKey_State_State"                   to "val value =  remember(externalState.value      ) {                  externalState.value         }",
    "RememberKey_State_State10"                 to "val value =  remember(externalState.value      ) {                  externalState.value  / 10   }",
    "RememberKey_State10_State10"               to "val value =  remember(externalState.value  / 10) {                  externalState.value  / 10   }",
    "RememberKey_byState_byState"               to "val value =  remember(externalByState          ) {                  externalByState             }",
    "RememberKey_byState_byState10"             to "val value =  remember(externalByState          ) {                  externalByState      / 10   }",
    "RememberKey_byState10_byState10"           to "val value =  remember(externalByState      / 10) {                  externalByState      / 10   }",
    "RememberKey_byStateValue_byStateValue"     to "val value =  remember(externalByStateValue     ) {                  externalByStateValue        }",
    "RememberKey_byStateValue_byStateValue10"   to "val value =  remember(externalByStateValue     ) {                  externalByStateValue / 10   }",
    "RememberKey_byStateValue10_byStateValue10" to "val value =  remember(externalByStateValue / 10) {                  externalByStateValue / 10   }",
    "RememberKey_Param_stateofParam"            to "val value by remember(param                    ) { mutableStateOf ( param                     ) }",
    "RememberKey_Param_stateofParam10"          to "val value by remember(param                    ) { mutableStateOf ( param                / 10 ) }",
    "RememberKey_Param10_stateofParam10"        to "val value by remember(param                / 10) { mutableStateOf ( param                / 10 ) }",
    "RememberKey_Param_Param"                   to "val value =  remember(param                    ) { param                                        }",
    "RememberKey_Param_Param10"                 to "val value =  remember(param                    ) { param                                 / 10   }",
    "RememberKey_Param10_Param10"               to "val value =  remember(param                / 10) { param                                 / 10   }",
    "Param"                                     to "val value =                                        param                                         ",
    "Param10"                                   to "val value =                                        param                                 / 10    ",
)

val functions = composables.map { (name, prepare) ->

    val args = if (prepare.matches(Regex("""param"""))) "(param: Int)" else "()"

    val prepare = prepare.replace(Regex(""" +"""), " ")

    """
    @Composable
    fun ${name}${args} {
        ${prepare}
        Row {
            Text(
                text = "   ${'$'}value   ",
                textAlign = TextAlign.End,
                modifier = Modifier
                    .width(70.dp)
                    .recomposeHighlighter()
            )
            Text("  ${name}", modifier = Modifier.recomposeHighlighter())
            Spacer(modifier = Modifier.weight(1f))
        }
    }
    """
}.joinToString("\n")

val indent = "        "

val invocations = composables.map { (name, prepare) ->

    val args = if (prepare.matches(Regex("""param"""))) "(externalValue)" else "()"

    indent + "#{name}#{args}"

}.joinToString("\n")

var text = File(file_template).readText()

File(file_backup).writeText(text)

text = text.replace(
    Regex("""(\n *//functions-begin *\n).*(\n *//functions-end *\n)""", option = RegexOption.MULTILINE),
    """\1""" + """\n""" + functions + """\2"""
    )

text = text.replace(
    Regex("""(\n *//invocations-begin *\n).*(\n *//invocations-end *\n)""", option = RegexOption.MULTILINE),
    """\1""" + """\n        Text("")\n\n""" + invocations + """\n\n        Text("")\n        Text("generated: ${time}")\n""" + """\2"""
    )

println(text)

File(file).writeText(text)
