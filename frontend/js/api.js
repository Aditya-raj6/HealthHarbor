// Shared helpers used by every page.

// Address of each microservice. Change a port here if you ever change it in application.properties.
const API = {
  resident: "http://localhost:8081",
  centre: "http://localhost:8082",
  inventory: "http://localhost:8083",
  camp: "http://localhost:8084",
  sanitation: "http://localhost:8085",
};

// ---------- talking to the REST APIs ----------

async function request(method, url, body) {
  let res;
  try {
    res = await fetch(url, {
      method: method,
      headers: body ? { "Content-Type": "application/json" } : {},
      body: body ? JSON.stringify(body) : undefined,
    });
  } catch (e) {
    throw new Error("Cannot reach " + new URL(url).origin + ". Start that service and try again.");
  }
  if (!res.ok) {
    let message = "The request failed (" + res.status + ").";
    try {
      const data = await res.json();
      if (data.error) message = data.error;
    } catch (e) { /* no JSON body, keep the default message */ }
    throw new Error(message);
  }
  const text = await res.text();
  return text ? JSON.parse(text) : null;
}

const http = {
  get: (url) => request("GET", url),
  post: (url, body) => request("POST", url, body || {}),
  put: (url, body) => request("PUT", url, body || {}),
  del: (url) => request("DELETE", url),
};

// ---------- small utilities ----------

// Makes text safe to put inside HTML
function esc(value) {
  return String(value == null ? "" : value).replace(/[&<>"']/g, (c) => (
    { "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[c]
  ));
}

const MONTHS = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];

// "2026-10-09" -> "9 Oct 2026" (non-breaking spaces so a date never splits across two lines)
function fmtDate(d) {
  if (!d) return "-";
  const p = String(d).split("-");
  return Number(p[2]) + " " + MONTHS[Number(p[1]) - 1] + " " + p[0];
}

// "09:00:00" -> "09:00"
function fmtTime(t) {
  return t ? String(t).slice(0, 5) : "-";
}

// Today's date as YYYY-MM-DD in the user's own time zone
function today() {
  const d = new Date();
  return d.getFullYear() + "-" + String(d.getMonth() + 1).padStart(2, "0") + "-" + String(d.getDate()).padStart(2, "0");
}

// Coloured status label. The colour comes from the status word.
const BADGES = {
  good: ["COMPLETED", "RESOLVED", "SAFE", "GOOD", "DONE", "DELIVERED", "APPROVED", "OK", "ONLINE"],
  wait: ["PENDING", "AVERAGE", "MEDIUM"],
  prog: ["IN_PROGRESS", "SCHEDULED", "PLANNED"],
  bad: ["HIGH", "UNSAFE", "POOR", "LOW STOCK", "CANCELLED", "EXPIRED"],
};

function badge(status) {
  const key = String(status == null ? "" : status).toUpperCase();
  let cls = "b-none";
  for (const name in BADGES) {
    if (BADGES[name].includes(key)) {
      cls = { good: "b-good", wait: "b-wait", prog: "b-prog", bad: "b-bad" }[name];
    }
  }
  const text = key.replace(/_/g, " ").toLowerCase();
  return '<span class="badge ' + cls + '">' + esc(text.charAt(0).toUpperCase() + text.slice(1)) + "</span>";
}

// Turns a <form> into a plain object. Empty fields become null, "...Id" fields become numbers.
function formData(form) {
  const out = {};
  new FormData(form).forEach((value, key) => {
    const text = String(value).trim();
    const field = form.elements[key];
    if (text === "") out[key] = null;
    else if (/Id$/.test(key) || (field && field.type === "number")) out[key] = Number(text);
    else out[key] = text;
  });
  return out;
}

// Fills a <select> with options. valueOf/labelOf say what to show for each item.
function fillSelect(select, items, valueOf, labelOf, placeholder) {
  let html = placeholder ? '<option value="">' + esc(placeholder) + "</option>" : "";
  items.forEach((item) => {
    html += '<option value="' + esc(valueOf(item)) + '">' + esc(labelOf(item)) + "</option>";
  });
  select.innerHTML = html;
}

// ---------- dialogs and messages ----------

function openDlg(id) { document.getElementById(id).showModal(); }
function closeDlg(id) { document.getElementById(id).close(); }

function toast(message, type) {
  let box = document.getElementById("toasts");
  if (!box) {
    box = document.createElement("div");
    box.id = "toasts";
    document.body.appendChild(box);
  }
  const el = document.createElement("div");
  el.className = "toast" + (type === "err" ? " err" : "");
  el.textContent = message;
  box.appendChild(el);
  setTimeout(() => el.remove(), type === "err" ? 6000 : 3500);
}

function showError(e) { toast(e.message || String(e), "err"); }

// ---------- user roles ----------
// DEMO-LEVEL access control: the chosen role is kept in the browser tab and decides which pages
// and buttons are shown. It does not check a password, so it is not real security.

const ROLES = {
  worker: {
    label: "Health Worker",
    info: "Register residents, log visits, report issues",
    pages: ["index", "residents", "centres", "appointments", "camps", "medicines", "sanitation", "water"],
    can: ["residents.edit", "appointments.edit", "camps.edit", "medicines.request", "sanitation.report"],
  },
  inventory: {
    label: "Inventory Manager",
    info: "Manage medicine stock and replenishment",
    pages: ["index", "centres", "medicines"],
    can: ["medicines.manage", "medicines.request"],
  },
  sanitation: {
    label: "Sanitation Officer",
    info: "Manage facilities, complaints, water quality",
    pages: ["index", "sanitation", "water"],
    can: ["sanitation.manage", "sanitation.report", "water.edit"],
  },
  admin: {
    label: "Administrator",
    info: "Full access, dashboards and reports",
    pages: ["index", "residents", "centres", "appointments", "medicines", "camps", "sanitation", "water"],
    can: ["*"],   // * means everything
  },
};

// Which permission each button or form needs. The key is the function the button calls.
// A function that is not listed here is open to everyone who can see the page.
const NEEDS = {
  addResident: "residents.edit", editResident: "residents.edit", removeResident: "residents.delete",
  saveResident: "residents.edit", saveVisit: "residents.edit",

  addCentre: "centres.edit", editCentre: "centres.edit", saveCentre: "centres.edit", saveWorker: "centres.edit",

  addAppointment: "appointments.edit", cancelAppointment: "appointments.edit",
  saveAppointment: "appointments.edit", saveConsultation: "appointments.edit",

  addCamp: "camps.edit", setStatus: "camps.edit", saveCamp: "camps.edit",
  saveStaff: "camps.edit", savePerson: "camps.edit", followUpDone: "camps.edit",

  addMedicine: "medicines.manage", editMedicine: "medicines.manage", giveOut: "medicines.manage",
  setRefill: "medicines.manage", saveMedicine: "medicines.manage", saveIssue: "medicines.manage",
  askRefill: "medicines.request", saveRefill: "medicines.request", requestRefill: "medicines.request",

  addFacility: "sanitation.manage", editFacility: "sanitation.manage", saveFacility: "sanitation.manage",
  startWork: "sanitation.manage", resolve: "sanitation.manage",
  addComplaint: "sanitation.report", saveComplaint: "sanitation.report", reportComplaint: "sanitation.report",

  addRecord: "water.edit", markDone: "water.edit", saveRecord: "water.edit",
};

function currentRole() {
  try {
    const key = sessionStorage.getItem("role");
    return ROLES[key] ? key : null;
  } catch (e) { return null; }
}

function setRole(key) {
  try { sessionStorage.setItem("role", key); } catch (e) { /* storage blocked, the role just will not be remembered */ }
}

function signOut() {
  try { sessionStorage.removeItem("role"); } catch (e) { /* nothing to remove */ }
  location.href = "login.html";
}

// Can the signed-in user do this? e.g. can("medicines.manage")
function can(permission) {
  const key = currentRole();
  if (!key) return false;
  const list = ROLES[key].can;
  return list.includes("*") || list.includes(permission);
}

// Can the signed-in user open this page? e.g. canOpen("medicines") or canOpen("medicines.html")
function canOpen(page) {
  const key = currentRole();
  return !!key && ROLES[key].pages.includes(String(page).replace(".html", ""));
}

// Hides every button or form the current role is not allowed to use.
function applyRole() {
  document.querySelectorAll("[onclick], [onsubmit]").forEach((el) => {
    const code = el.getAttribute("onclick") || el.getAttribute("onsubmit") || "";
    const match = code.match(/^\s*([A-Za-z_$][\w$]*)\s*\(/);
    const needed = match && NEEDS[match[1]];
    if (needed && !can(needed)) el.style.display = "none";
  });
}

// ---------- sidebar ----------

const NAV = [
  { group: "Overview" },
  { id: "index", label: "Dashboard", href: "index.html" },
  { group: "Healthcare" },
  { id: "residents", label: "Residents", href: "residents.html" },
  { id: "centres", label: "Health centres", href: "centres.html" },
  { id: "appointments", label: "Appointments", href: "appointments.html" },
  { id: "medicines", label: "Medicines and stock", href: "medicines.html" },
  { id: "camps", label: "Health camps", href: "camps.html" },
  { group: "Environment" },
  { id: "sanitation", label: "Sanitation", href: "sanitation.html" },
  { id: "water", label: "Water quality", href: "water.html" },
];

function renderNav(active) {
  const role = currentRole();
  if (!role) { location.replace("login.html"); return; }        // not signed in
  if (!canOpen(active)) { location.replace("index.html"); return; } // this role may not open this page

  let html = '<div class="brand"><strong>HealthHarbor</strong><span>Rural health and sanitation</span></div><nav>';
  let pendingGroup = "";
  NAV.forEach((item) => {
    if (item.group) {
      pendingGroup = '<div class="nav-group">' + item.group + "</div>";   // shown only if a page below it is allowed
    } else if (canOpen(item.id)) {
      html += pendingGroup;
      pendingGroup = "";
      html += '<a class="nav-link' + (item.id === active ? " active" : "") + '" href="' + item.href + '">' + item.label + "</a>";
    }
  });
  html += '</nav><div class="whoami"><span>Signed in as</span><strong>' + esc(ROLES[role].label) + "</strong>" +
          '<a href="#" onclick="signOut(); return false;">Switch role</a></div>';
  document.getElementById("nav").innerHTML = html;

  // buttons are created later by each page, so watch for new ones and hide what this role cannot use
  applyRole();
  new MutationObserver(applyRole).observe(document.body, { childList: true, subtree: true });
}
