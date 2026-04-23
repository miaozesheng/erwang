import test from 'node:test'
import assert from 'node:assert/strict'

import { calculateExpandedEditorHeight } from './editorLayout.js'

test('uses the sticky header bottom when editor top scrolls underneath it', () => {
  const height = calculateExpandedEditorHeight({
    viewportHeight: 900,
    headerBottom: 96,
    editorTop: 72,
    bottomGap: 24,
    minHeight: 360
  })

  assert.equal(height, 780)
})

test('uses the editor top when it starts below the sticky header', () => {
  const height = calculateExpandedEditorHeight({
    viewportHeight: 900,
    headerBottom: 96,
    editorTop: 220,
    bottomGap: 24,
    minHeight: 360
  })

  assert.equal(height, 656)
})

test('falls back to the provided minimum height when space is too tight', () => {
  const height = calculateExpandedEditorHeight({
    viewportHeight: 520,
    headerBottom: 92,
    editorTop: 320,
    bottomGap: 24,
    minHeight: 360
  })

  assert.equal(height, 360)
})

test('normalizes invalid inputs before calculating the height', () => {
  const height = calculateExpandedEditorHeight({
    viewportHeight: Number.NaN,
    headerBottom: -12,
    editorTop: -40,
    bottomGap: -8,
    minHeight: 320
  })

  assert.equal(height, 320)
})
