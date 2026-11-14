/**
 * Warm-Flow 1.8.9 compatibility shim.
 *
 * The Vue renderer can pass a CSS drop-shadow() string object to SVG
 * <feDropShadow>, which expands into attribute names such as "0" and causes
 * InvalidCharacterError. Ignore only invalid DOM attribute names so the rest
 * of the designer can render normally.
 */
(function () {
  const nativeSetAttribute = Element.prototype.setAttribute;
  const nativeSetAttributeNS = Element.prototype.setAttributeNS;
  const validAttributeName = /^[A-Za-z_:][-A-Za-z0-9_:.]*$/;
  const SVG_NS = 'http://www.w3.org/2000/svg';
  const INVALID_POSITION_ATTRIBUTE = 'data-warm-flow-invalid-position';

  /**
   * The 1.8.9 MIMIC node uses a percentage-width HTML card inside a
   * foreignObject. Its content-box top section can grow wider than the card
   * when the iframe is resized or the host uses a non-default browser zoom.
   * Pin both sections to the card width and reserve exactly one 25px header.
   */
  function installMimicLayoutFix() {
    const styleId = 'warm-flow-mimic-layout-fix';
    if (document.getElementById(styleId)) {
      return;
    }
    const style = document.createElement('style');
    style.id = styleId;
    style.textContent = `
      .base-node {
        position: relative !important;
        display: block !important;
        width: 100% !important;
        max-width: 100% !important;
        height: 80px !important;
        margin: 0 !important;
        padding: 0 !important;
        box-sizing: border-box !important;
        overflow: hidden !important;
      }
      .base-node .top-section {
        position: absolute !important;
        top: 0 !important;
        right: 0 !important;
        bottom: auto !important;
        left: 0 !important;
        display: flex !important;
        align-items: center !important;
        width: auto !important;
        max-width: none !important;
        height: 25px !important;
        min-width: 0 !important;
        margin: 0 !important;
        padding: 0 10px !important;
        box-sizing: border-box !important;
        overflow: hidden !important;
        transform: none !important;
      }
      .base-node .top-section > span:first-child {
        min-width: 0;
        max-width: 100%;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
      .base-node .top-section > input {
        width: 100% !important;
        min-width: 0 !important;
        box-sizing: border-box !important;
      }
      .base-node .bottom-section {
        position: absolute !important;
        top: 25px !important;
        right: 0 !important;
        bottom: 0 !important;
        left: 0 !important;
        width: auto !important;
        max-width: none !important;
        height: auto !important;
        min-height: 0 !important;
        margin: 0 !important;
        box-sizing: border-box !important;
        overflow: hidden !important;
        white-space: nowrap;
        text-overflow: ellipsis;
        transform: none !important;
      }
    `;
    (document.head || document.documentElement).appendChild(style);
  }

  installMimicLayoutFix();

  /**
   * Vue applies the node background as an inline style. Pin the structural
   * properties inline as well so a scoped stylesheet, browser zoom, or a late
   * Vue update cannot resize the header independently from the card.
   */
  function pinMimicNodeLayout(root) {
    const pin = (element, property, value) => {
      if (element.style.getPropertyValue(property) !== value
        || element.style.getPropertyPriority(property) !== 'important') {
        element.style.setProperty(property, value, 'important');
      }
    };

    root.querySelectorAll('.base-node').forEach((node) => {
      // WebKit paints the transparent SVG backing rect of a MIMIC node at a
      // different position from its sibling foreignObject, which leaves a
      // second blank card behind the real HTML card. The HTML card already
      // provides the visible surface, shadow and hit area.
      const foreignObject = node.closest('foreignObject');
      const backingRect = foreignObject
        ? foreignObject.parentElement.querySelector(':scope > rect')
        : null;
      if (backingRect) {
        pin(backingRect, 'visibility', 'hidden');
        pin(backingRect, 'pointer-events', 'none');
      }

      pin(node, 'position', 'relative');
      pin(node, 'display', 'block');
      pin(node, 'width', '100%');
      pin(node, 'max-width', '100%');
      pin(node, 'height', '80px');
      pin(node, 'margin', '0');
      pin(node, 'padding', '0');
      pin(node, 'box-sizing', 'border-box');
      pin(node, 'overflow', 'hidden');

      const top = node.querySelector(':scope > .top-section');
      if (top) {
        pin(top, 'position', 'absolute');
        pin(top, 'top', '0');
        pin(top, 'right', '0');
        pin(top, 'bottom', 'auto');
        pin(top, 'left', '0');
        pin(top, 'display', 'flex');
        pin(top, 'align-items', 'center');
        pin(top, 'width', 'auto');
        pin(top, 'max-width', 'none');
        pin(top, 'height', '25px');
        pin(top, 'min-width', '0');
        pin(top, 'margin', '0');
        pin(top, 'padding', '0 10px');
        pin(top, 'box-sizing', 'border-box');
        pin(top, 'overflow', 'hidden');
        pin(top, 'transform', 'none');

        const title = top.querySelector(':scope > span:first-child');
        if (title) {
          pin(title, 'min-width', '0');
          pin(title, 'max-width', '100%');
          pin(title, 'overflow', 'hidden');
          pin(title, 'white-space', 'nowrap');
          pin(title, 'text-overflow', 'ellipsis');
        }

        const input = top.querySelector(':scope > input');
        if (input) {
          pin(input, 'width', '100%');
          pin(input, 'min-width', '0');
          pin(input, 'box-sizing', 'border-box');
        }
      }

      const bottom = node.querySelector(':scope > .bottom-section');
      if (bottom) {
        pin(bottom, 'position', 'absolute');
        pin(bottom, 'top', '25px');
        pin(bottom, 'right', '0');
        pin(bottom, 'bottom', '0');
        pin(bottom, 'left', '0');
        pin(bottom, 'width', 'auto');
        pin(bottom, 'max-width', 'none');
        pin(bottom, 'height', 'auto');
        pin(bottom, 'min-height', '0');
        pin(bottom, 'margin', '0');
        pin(bottom, 'box-sizing', 'border-box');
        pin(bottom, 'overflow', 'hidden');
        pin(bottom, 'white-space', 'nowrap');
        pin(bottom, 'text-overflow', 'ellipsis');
        pin(bottom, 'transform', 'none');
      }
    });
  }

  /**
   * WebKit paints HTML inside a MIMIC node's foreignObject without applying
   * the graph group's SVG translate/scale transform. The SVG edges and add
   * buttons do apply it, so the cards look centred in the DOM while being
   * painted away from the connector in Safari.
   *
   * Apply the missing foreignObject CTM to its direct HTML wrapper. This is
   * deliberately limited to Apple's WebKit engine; Chromium already applies
   * the ancestor transform and would otherwise be transformed twice.
   */
  const isAppleWebKit = navigator.vendor === 'Apple Computer, Inc.'
    && typeof window.webkitConvertPointFromNodeToPage === 'function';

  function syncMimicForeignObjectTransform(root) {
    if (!isAppleWebKit) {
      return;
    }

    root.querySelectorAll('foreignObject').forEach((foreignObject) => {
      if (!foreignObject.querySelector('.base-node')) {
        return;
      }

      const wrapper = foreignObject.firstElementChild;
      if (!wrapper || typeof foreignObject.getCTM !== 'function') {
        return;
      }

      let matrix;
      try {
        matrix = foreignObject.getCTM();
      } catch (error) {
        return;
      }
      if (!matrix
        || ![matrix.a, matrix.b, matrix.c, matrix.d, matrix.e, matrix.f]
          .every(Number.isFinite)) {
        return;
      }

      const round = (value) => Math.abs(value) < 0.000001 ? 0 : Number(value.toFixed(6));
      const transform = `matrix(${round(matrix.a)}, ${round(matrix.b)}, ${round(matrix.c)}, ${round(matrix.d)}, ${round(matrix.e)}, ${round(matrix.f)})`;
      if (wrapper.style.getPropertyValue('transform') !== transform
        || wrapper.style.getPropertyPriority('transform') !== 'important') {
        wrapper.style.setProperty('transform-origin', '0 0', 'important');
        wrapper.style.setProperty('transform-box', 'border-box', 'important');
        wrapper.style.setProperty('transform', transform, 'important');
      }
    });
  }

  function isInvalidSvgPosition(element, name, value) {
    return element.namespaceURI === SVG_NS
      && element.tagName.toLowerCase() === 'text'
      && (name === 'x' || name === 'y')
      && String(value) === 'NaN';
  }

  function markInvalidSvgPosition(element) {
    nativeSetAttribute.call(element, INVALID_POSITION_ATTRIBUTE, 'true');
  }

  Element.prototype.setAttribute = function (name, value) {
    if (!validAttributeName.test(String(name))) {
      return;
    }
    // The 1.8.9 classic designer initially writes x/y="NaN" before the node
    // geometry is available. Suppress only that transient value, then let the
    // mutation observer resolve the real centre from the rendered shape.
    if (isInvalidSvgPosition(this, String(name), value)) {
      markInvalidSvgPosition(this);
      return;
    }
    return nativeSetAttribute.call(this, name, value);
  };

  Element.prototype.setAttributeNS = function (namespace, name, value) {
    if (!validAttributeName.test(String(name))) {
      return;
    }
    if (isInvalidSvgPosition(this, String(name), value)) {
      markInvalidSvgPosition(this);
      return;
    }
    return nativeSetAttributeNS.call(this, namespace, name, value);
  };

  /**
   * Warm-Flow 1.8.9 can render classic gateway text with x/y="NaN".
   * The text node and its gateway shape are siblings, but the shape lives in a
   * transformed group. Resolve the shape's visual centre in the text parent's
   * coordinate system so the label stays centred through zoom and pan.
   */
  function fixInvalidClassicText(text) {
    if (!text || text.tagName.toLowerCase() !== 'text') {
      return;
    }
    const invalidPosition = text.getAttribute('x') === 'NaN'
      || text.getAttribute('y') === 'NaN'
      || text.hasAttribute(INVALID_POSITION_ATTRIBUTE);
    if (!invalidPosition) {
      return;
    }

    const node = text.closest('.lf-node');
    const textLayer = text.parentElement;
    if (!node || !textLayer || !textLayer.getCTM) {
      return;
    }

    const shapes = Array.from(node.querySelectorAll('polygon, rect, circle, path'))
      .filter((shape) => !shape.closest('defs'))
      .map((shape) => {
        try {
          const box = shape.getBBox();
          return { shape, box, area: box.width * box.height };
        } catch (error) {
          return null;
        }
      })
      .filter((item) => item && item.area > 0)
      .sort((left, right) => right.area - left.area);

    if (!shapes.length) {
      return;
    }

    const { shape, box } = shapes[0];
    const point = shape.ownerSVGElement.createSVGPoint();
    point.x = box.x + box.width / 2;
    point.y = box.y + box.height / 2;

    try {
      const local = point
        .matrixTransform(shape.getCTM())
        .matrixTransform(textLayer.getCTM().inverse());
      if (!Number.isFinite(local.x) || !Number.isFinite(local.y)) {
        return;
      }
      nativeSetAttribute.call(text, 'x', String(local.x));
      nativeSetAttribute.call(text, 'y', String(local.y));
      text.removeAttribute(INVALID_POSITION_ATTRIBUTE);
    } catch (error) {
      // The graph may be detached while LogicFlow is replacing its DOM.
    }
  }

  /**
   * Imported classic gateway labels can carry a text position from the source
   * model, which puts long labels directly over the 50px diamond and its icon.
   * Place those labels to the right of the shape and give them a small canvas
   * coloured backing so they remain readable while the graph is panned/zoomed.
   */
  function fixGatewayLabelOverflow(node) {
    const polygon = Array.from(node.querySelectorAll('polygon'))
      .find((shape) => shape.getAttribute('points') === '25,0,50,25,25,50,0,25');
    if (!polygon) {
      return;
    }

    const text = Array.from(node.querySelectorAll('text'))
      .find((candidate) => !candidate.closest('.lf-line-text'));
    if (!text) {
      return;
    }

    const centerX = Number(polygon.getAttribute('x'));
    const centerY = Number(polygon.getAttribute('y'));
    if (!Number.isFinite(centerX) || !Number.isFinite(centerY)) {
      return;
    }

    const targetX = centerX + 67;
    const targetY = centerY;
    if (Number(text.getAttribute('x')) !== targetX) {
      nativeSetAttribute.call(text, 'x', String(targetX));
    }
    if (Number(text.getAttribute('y')) !== targetY) {
      nativeSetAttribute.call(text, 'y', String(targetY));
    }

    const parent = text.parentElement;
    if (!parent) {
      return;
    }
    let background = parent.querySelector('rect[data-warm-flow-gateway-label]');
    if (!background) {
      background = document.createElementNS(SVG_NS, 'rect');
      nativeSetAttribute.call(background, 'data-warm-flow-gateway-label', 'true');
      nativeSetAttribute.call(background, 'rx', '4');
      nativeSetAttribute.call(background, 'pointer-events', 'none');
      parent.insertBefore(background, text);
    }

    let box;
    try {
      box = text.getBBox();
    } catch (error) {
      return;
    }
    const fill = document.documentElement.classList.contains('dark') ? '#181a20' : '#ffffff';
    const attributes = {
      x: box.x - 4,
      y: box.y - 3,
      width: box.width + 8,
      height: box.height + 6,
      fill,
    };
    Object.entries(attributes).forEach(([name, value]) => {
      const nextValue = String(value);
      if (background.getAttribute(name) !== nextValue) {
        nativeSetAttribute.call(background, name, nextValue);
      }
    });
  }

  /**
   * In mimic mode the first add button is initially placed on the target
   * node's top edge. The HTML node is painted above the SVG edge, so the "+"
   * becomes invisible and cannot receive mouseenter. Move only occluded add
   * buttons into the visual gap between the adjacent upper/lower nodes.
   */
  function fixOccludedMimicAddButtons(root) {
    const nodeRects = Array.from(document.querySelectorAll('.lf-node'))
      .map((node) => ({ node, rect: node.getBoundingClientRect() }))
      .filter(({ rect }) => rect.width > 0 && rect.height > 0);

    const addButtons = Array.from(root.querySelectorAll('g')).filter((group) => {
      const circle = group.querySelector(':scope > circle[fill="#409eff"]');
      const lines = group.querySelectorAll(':scope > line[stroke="white"]');
      return circle && lines.length === 2;
    });

    addButtons.forEach((group) => {
      const circle = group.querySelector(':scope > circle[fill="#409eff"]');
      if (!circle || !circle.ownerSVGElement) {
        return;
      }
      const rect = circle.getBoundingClientRect();
      const centerX = rect.left + rect.width / 2;
      const centerY = rect.top + rect.height / 2;
      const overlapped = nodeRects.some(({ rect: nodeRect }) =>
        centerX >= nodeRect.left && centerX <= nodeRect.right &&
        centerY >= nodeRect.top && centerY <= nodeRect.bottom
      );
      if (!overlapped) {
        return;
      }

      const aligned = nodeRects.filter(({ rect: nodeRect }) =>
        centerX >= nodeRect.left - 2 && centerX <= nodeRect.right + 2
      );
      const upper = aligned
        .filter(({ rect: nodeRect }) => nodeRect.bottom <= centerY + 2)
        .sort((left, right) => right.rect.bottom - left.rect.bottom)[0];
      const lower = aligned
        .filter(({ rect: nodeRect }) => nodeRect.top >= centerY - 2)
        .sort((left, right) => left.rect.top - right.rect.top)[0];

      if (!upper || !lower || lower.rect.top <= upper.rect.bottom) {
        return;
      }

      const targetScreenY = (upper.rect.bottom + lower.rect.top) / 2;
      if (Math.abs(targetScreenY - centerY) < 3) {
        return;
      }

      try {
        const svg = circle.ownerSVGElement;
        const point = svg.createSVGPoint();
        point.x = centerX;
        point.y = targetScreenY;
        const local = point.matrixTransform(circle.getScreenCTM().inverse());
        const oldCenterY = Number(circle.getAttribute('cy'));
        if (!Number.isFinite(local.y) || !Number.isFinite(oldCenterY)) {
          return;
        }
        const deltaY = local.y - oldCenterY;
        nativeSetAttribute.call(circle, 'cy', String(local.y));
        group.querySelectorAll(':scope > line').forEach((line) => {
          const y1 = Number(line.getAttribute('y1'));
          const y2 = Number(line.getAttribute('y2'));
          if (Number.isFinite(y1)) {
            nativeSetAttribute.call(line, 'y1', String(y1 + deltaY));
          }
          if (Number.isFinite(y2)) {
            nativeSetAttribute.call(line, 'y2', String(y2 + deltaY));
          }
        });
      } catch (error) {
        // Ignore transient SVG geometry while the designer is re-rendering.
      }
    });
  }

  let graphFixScheduled = false;
  function scheduleGraphFix() {
    if (graphFixScheduled) {
      return;
    }
    graphFixScheduled = true;
    requestAnimationFrame(() => {
      graphFixScheduled = false;
      pinMimicNodeLayout(document);
      syncMimicForeignObjectTransform(document);
      document.querySelectorAll('text[x="NaN"], text[y="NaN"], text[data-warm-flow-invalid-position]')
        .forEach(fixInvalidClassicText);
      document.querySelectorAll('.lf-node').forEach(fixGatewayLabelOverflow);
      fixOccludedMimicAddButtons(document);
    });
  }

  /**
   * The active/inactive state is the application's edit boundary. An inactive
   * flow may still have isPublish=1, so mark definition queries as editable and
   * let the backend relax only the designer's publish read-only flag.
   */
  const pageParams = new URLSearchParams(window.location.search);
  const editableDesigner = pageParams.get('onlyDesignShow') === 'true' && pageParams.get('disabled') !== 'true';
  const nativeOpen = XMLHttpRequest.prototype.open;
  const nativeSend = XMLHttpRequest.prototype.send;
  const nativeSetRequestHeader = XMLHttpRequest.prototype.setRequestHeader;

  XMLHttpRequest.prototype.open = function (method, url) {
    this.__warmFlowEditableQuery = editableDesigner && /\/warm-flow\/query-def(?:\/|$)/.test(String(url));
    return nativeOpen.apply(this, arguments);
  };

  XMLHttpRequest.prototype.send = function (body) {
    if (this.__warmFlowEditableQuery) {
      nativeSetRequestHeader.call(this, 'X-WarmFlow-Editable', 'true');
    }
    return nativeSend.call(this, body);
  };

  new MutationObserver(scheduleGraphFix).observe(document.documentElement, {
    childList: true,
    subtree: true,
    attributes: true,
    attributeFilter: ['x', 'y', 'cy', 'transform', 'class', 'style', INVALID_POSITION_ATTRIBUTE],
  });
  document.addEventListener('DOMContentLoaded', scheduleGraphFix, { once: true });
  scheduleGraphFix();
})();
