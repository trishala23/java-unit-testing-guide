(function () {
  "use strict";

  var root = document.documentElement;
  var STORAGE_KEY = "jutg-theme";

  function applyTheme(theme) {
    if (theme === "dark" || theme === "light") {
      root.setAttribute("data-theme", theme);
    } else {
      root.removeAttribute("data-theme");
    }
    var toggle = document.getElementById("theme-toggle");
    var effectiveDark = theme === "dark" || (!theme && window.matchMedia("(prefers-color-scheme: dark)").matches);
    if (toggle) {
      toggle.textContent = effectiveDark ? "☀️" : "🌙";
      toggle.setAttribute("aria-label", effectiveDark ? "Switch to light theme" : "Switch to dark theme");
    }
    var hljsTheme = document.getElementById("hljs-theme");
    if (hljsTheme) {
      hljsTheme.href = effectiveDark
        ? "https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.9.0/styles/github-dark.min.css"
        : "https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.9.0/styles/github.min.css";
    }
  }

  var stored = null;
  try {
    stored = localStorage.getItem(STORAGE_KEY);
  } catch (e) {
    /* private browsing / storage disabled — fall back to system preference */
  }
  applyTheme(stored);

  document.addEventListener("DOMContentLoaded", function () {
    var toggle = document.getElementById("theme-toggle");
    if (toggle) {
      toggle.addEventListener("click", function () {
        var current = root.getAttribute("data-theme");
        var isDark = current === "dark" || (!current && window.matchMedia("(prefers-color-scheme: dark)").matches);
        var next = isDark ? "light" : "dark";
        applyTheme(next);
        try {
          localStorage.setItem(STORAGE_KEY, next);
        } catch (e) {
          /* ignore */
        }
      });
    }

    // Mobile sidebar toggle
    var navToggle = document.querySelector(".nav-toggle");
    var sidebar = document.getElementById("sidebar");
    var scrim = document.querySelector(".sidebar-scrim");
    function closeSidebar() {
      if (sidebar) sidebar.classList.remove("open");
      if (scrim) scrim.classList.remove("open");
      if (navToggle) navToggle.setAttribute("aria-expanded", "false");
    }
    function openSidebar() {
      if (sidebar) sidebar.classList.add("open");
      if (scrim) scrim.classList.add("open");
      if (navToggle) navToggle.setAttribute("aria-expanded", "true");
    }
    if (navToggle && sidebar) {
      navToggle.addEventListener("click", function () {
        var isOpen = sidebar.classList.contains("open");
        if (isOpen) closeSidebar(); else openSidebar();
      });
    }
    if (scrim) scrim.addEventListener("click", closeSidebar);

    // Syntax highlighting (highlight.js is loaded via CDN in the page head)
    if (window.hljs) {
      document.querySelectorAll("pre code").forEach(function (block) {
        window.hljs.highlightElement(block);
      });
    }

    // Wrap code blocks so a "Copy" button can be absolutely positioned,
    // then wire it up.
    document.querySelectorAll(".prose pre").forEach(function (pre) {
      var wrap = document.createElement("div");
      wrap.className = "code-block-wrap";
      pre.parentNode.insertBefore(wrap, pre);
      wrap.appendChild(pre);

      var btn = document.createElement("button");
      btn.type = "button";
      btn.className = "copy-btn";
      btn.textContent = "Copy";
      btn.addEventListener("click", function () {
        var code = pre.querySelector("code");
        var text = code ? code.innerText : pre.innerText;
        var done = function () {
          btn.textContent = "Copied!";
          btn.classList.add("copied");
          setTimeout(function () {
            btn.textContent = "Copy";
            btn.classList.remove("copied");
          }, 1600);
        };
        if (navigator.clipboard && navigator.clipboard.writeText) {
          navigator.clipboard.writeText(text).then(done, done);
        } else {
          var ta = document.createElement("textarea");
          ta.value = text;
          ta.style.position = "fixed";
          ta.style.opacity = "0";
          document.body.appendChild(ta);
          ta.select();
          try { document.execCommand("copy"); } catch (e) { /* ignore */ }
          document.body.removeChild(ta);
          done();
        }
      });
      wrap.appendChild(btn);
    });

    // Highlight the current section in the "On this page" rail while scrolling
    var tocLinks = Array.prototype.slice.call(document.querySelectorAll(".toc-rail a"));
    if (tocLinks.length) {
      var headings = tocLinks
        .map(function (a) { return document.getElementById(a.getAttribute("href").slice(1)); })
        .filter(Boolean);

      var setActive = function (id) {
        tocLinks.forEach(function (a) {
          a.style.borderLeftColor = a.getAttribute("href") === "#" + id ? "var(--brand)" : "transparent";
          a.style.color = a.getAttribute("href") === "#" + id ? "var(--text)" : "";
        });
      };

      if ("IntersectionObserver" in window) {
        var observer = new IntersectionObserver(
          function (entries) {
            entries.forEach(function (entry) {
              if (entry.isIntersecting) setActive(entry.target.id);
            });
          },
          { rootMargin: "-15% 0px -70% 0px" }
        );
        headings.forEach(function (h) { observer.observe(h); });
      }
    }
  });
})();
