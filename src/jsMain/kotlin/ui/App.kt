package ui

import csstype.*
import mui.icons.material.Android
import mui.icons.material.Apple
import mui.icons.material.ArrowCircleDown
import mui.icons.material.Laptop
import mui.material.*
import mui.material.Size
import mui.material.styles.TypographyVariant
import mui.system.Breakpoint
import mui.system.responsive
import mui.system.sx
import react.*
import react.dom.html.ReactHTML
import react.dom.onChange
import web.html.HTMLInputElement
import wizard.*

external interface AppProps : Props {
    var generate: (ProjectInfo) -> Unit
}

val App = FC<AppProps> { props ->
    ThemeModule {
        Container {
            sx {
                padding = Padding(24.px, 24.px)
                minWidth = 650.px
            }
            Paper {
                val default = ProjectInfo()

                var projectName by useState(default.name)
                var projectId by useState(default.packageId)
                val withAndroidState = useState(default.platforms.contains(ComposePlatform.Android))
                val withIosState = useState(default.platforms.contains(ComposePlatform.Ios))
                val withDesktopState = useState(default.platforms.contains(ComposePlatform.Desktop))

                sx {
                    padding = Padding(24.px, 24.px)
                }

                Stack {
                    direction = responsive(StackDirection.column)
                    spacing = responsive(2)
                    sx {
                        alignItems = AlignItems.center
                    }

                    Stack {
                        direction = responsive(StackDirection.row)
                        spacing = responsive(2)
                        sx {
                            alignItems = AlignItems.center
                        }
                        ReactHTML.img {
                            src = "compose-logo.svg"
                            width = 150.0
                            height = 150.0
                        }
                        Typography {
                            variant = TypographyVariant.h3
                            +"Compose Multiplatform Wizard"
                        }
                    }

                    TextField {
                        label = ReactNode("Project name")
                        sx {
                            width = 424.px
                        }
                        value = projectName
                        onChange = { event ->
                            projectName = (event.target as HTMLInputElement).value
                        }
                    }

                    TextField {
                        label = ReactNode("Project ID")
                        sx {
                            width = 424.px
                        }
                        value = projectId
                        onChange = { event ->
                            projectId = (event.target as HTMLInputElement).value
                        }
                    }

                    ButtonGroup {
                        disableElevation = true
                        TargetButton {
                            selection = withAndroidState
                            icon = Android
                            title = "Android"
                        }
                        TargetButton {
                            selection = withIosState
                            icon = Apple
                            title = "iOS"
                        }
                        TargetButton {
                            selection = withDesktopState
                            icon = Laptop
                            title = "Desktop"
                        }
                    }

                    val deps = mapOf(
                        Napier to useState(true),
                        LibresCompose to useState(true),
                        Voyager to useState(true),
                        ImageLoader to useState(true),
                        KotlinxCoroutinesCore to useState(true),
                        KtorCore to useState(false),
                        KotlinxSerializationJson to useState(false),
                        KotlinxDateTime to useState(false),
                        MultiplatformSettings to useState(false),
                        Koin to useState(false),
                        KStore to useState(false),
                        SQLDelightPlugin to useState(false)
                    )
                    Grid {
                        sx {
                            justifyContent = JustifyContent.spaceAround
                        }
                        spacing = responsive(2)
                        container = true
                        deps.forEach { (dep, state) ->
                            Grid {
                                item = true
                                DependencyCard {
                                    dependency = dep
                                    selection = state
                                }
                            }
                        }
                    }

                    Button {
                        variant = ButtonVariant.contained
                        size = Size.large
                        startIcon = ArrowCircleDown.create()
                        +"Download"

                        val withAndroid by withAndroidState
                        val withIos by withIosState
                        val withDesktop by withDesktopState
                        disabled = projectName.isBlank()
                                || projectId.isBlank()
                                || (!withAndroid && !withIos && !withDesktop)

                        onClick = {
                            val info = ProjectInfo(
                                packageId = projectId,
                                name = projectName,
                                platforms = buildSet {
                                    if (withAndroid) add(ComposePlatform.Android)
                                    if (withIos) add(ComposePlatform.Ios)
                                    if (withDesktop) add(ComposePlatform.Desktop)
                                },
                                dependencies = requiredAndroidDependencies + getSelectedDependencies(deps)
                            )
                            props.generate(info)
                        }
                    }
                }
            }
        }
    }
}

private fun getSelectedDependencies(deps: Map<Dependency, StateInstance<Boolean>>) =
    deps
        .filter { (_, s) -> s.component1() }
        .flatMap { (d, _) ->
            when {
                d.group == "io.github.skeptick.libres" -> listOf(LibresPlugin, LibresCompose)
                d.group == "io.ktor" -> listOfNotNull(KtorCore, KtorClientDarwin, KtorClientOkhttp)
                d.group == "app.cash.sqldelight" -> listOf(
                    SQLDelightPlugin,
                    SQLDelightDriverJvm,
                    SQLDelightDriverAndroid,
                    SQLDelightDriverNative
                )

                d.id.contains("coroutines") -> listOf(KotlinxCoroutinesCore, KotlinxCoroutinesAndroid)
                d.id.contains("serialization") -> listOf(KotlinxSerializationPlugin, KotlinxSerializationJson)
                else -> listOf(d)
            }
        }
        .toSet()