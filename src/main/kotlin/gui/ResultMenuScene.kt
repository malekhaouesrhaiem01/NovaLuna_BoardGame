package gui

import service.Refreshable
import service.RootService
import tools.aqua.bgw.components.layoutviews.Pane
import tools.aqua.bgw.components.uicomponents.UIComponent
import tools.aqua.bgw.core.MenuScene

class ResultMenuScene (private val rootService: RootService) : MenuScene(1920, 1080), Refreshable {

    /**
     * Container pane for centering all UI components.
     */
    private val contentPane = Pane<UIComponent>(
        width = 600,
        height = 500,
        posX = 660,
        posY = 290,
    )


}