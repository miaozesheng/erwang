export const calculateExpandedEditorHeight = ({
  viewportHeight,
  headerBottom = 0,
  editorTop = 0,
  bottomGap = 0,
  minHeight = 360
}) => {
  const safeViewportHeight = Number.isFinite(viewportHeight) && viewportHeight > 0
    ? viewportHeight
    : minHeight
  const safeHeaderBottom = Number.isFinite(headerBottom) ? Math.max(headerBottom, 0) : 0
  const safeEditorTop = Number.isFinite(editorTop) ? Math.max(editorTop, 0) : 0
  const safeBottomGap = Number.isFinite(bottomGap) ? Math.max(bottomGap, 0) : 0
  const safeMinHeight = Number.isFinite(minHeight) && minHeight > 0 ? minHeight : 360

  const visibleTop = Math.max(safeHeaderBottom, safeEditorTop)
  const availableHeight = Math.floor(safeViewportHeight - visibleTop - safeBottomGap)

  return Math.max(safeMinHeight, availableHeight)
}
