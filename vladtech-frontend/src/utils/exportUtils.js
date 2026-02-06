import { jsPDF } from 'jspdf';
import autoTable from 'jspdf-autotable';
import logo from '../assets/vladtechlogo.png';
import i18n from '../i18n';

const formatMoney = (amount, currency, locale = 'en-CA') => {
  if (amount === null || amount === undefined || amount === "") return 'N/A';
  const num = typeof amount === 'number' ? amount : Number(amount);
  if (Number.isNaN(num)) return 'N/A';
  return new Intl.NumberFormat(locale, {
    style: 'currency',
    currency: currency || 'CAD',
  }).format(num);
};

const formatEmployees = (emails, noneLabel = '-') => {
  if (!emails || emails.length === 0) return noneLabel;
  return emails.filter(e => e && !e.startsWith('auth0|')).join(', ');
};

const formatAddress = (addr) => {
  if (!addr) return '-';
  const parts = [];
  if (addr.streetAddress) parts.push(addr.streetAddress);
  const cityProv = [addr.city, addr.province].filter(Boolean).join(' ');
  if (cityProv) parts.push(cityProv);
  return parts.length > 0 ? parts.join(', ') : '-';
};

export const generateCsv = (projects, filename = 'projects.csv', options = {}) => {
  if (!projects || projects.length === 0) {
    console.warn("generateCsv: No projects to export");
    return;
  }

  const { locale = 'en-CA' } = options;
  const lang = locale.split('-')[0];
  const t = (key) => i18n.t(key, { lng: lang });

  const headers = [
    t('project.id'), 
    t('project.projectName'), 
    t('project.client'), 
    t('project.employee'), 
    t('project.status'), 
    t('project.priority'), 
    t('project.projectType'), 
    t('project.estimatedCost'),
    t('project.startDate'),
    t('project.dueDate'),
    t('project.addressLabel')
  ];

  const rows = projects.map(p => {
    if (!p) return Array(headers.length).fill('');
    
    // Explicitly map IN_PROGRESS to the camelCase key used in i18n
    const mappedStatus = p.status === 'IN_PROGRESS' ? 'inProgress' : (p.status?.toLowerCase() || 'none');
    const statusKey = `project.${mappedStatus}`;
    
    const priorityKey = `project.priority${p.priority?.charAt(0).toUpperCase() + p.priority?.slice(1).toLowerCase() || 'none'}`;
    const typeKey = `admin.stats.${p.projectType?.toLowerCase() || 'none'}`;

    const address = formatAddress(p.address);
    
    return [
      p.projectIdentifier || '',
      p.name || '',
      p.clientName || '-',
      formatEmployees(p.assignedEmployeeEmails, '-'),
      t(statusKey).toUpperCase(),
      t(priorityKey).toUpperCase(),
      t(typeKey),
      p.estimatedCost ? `${p.estimatedCost} ${p.estimatedCostCurrency || 'CAD'}` : '-',
      p.startDate || '-',
      p.dueDate || '-',
      address
    ];
  });

  const csvString = [
    headers.join(','),
    ...rows.map(row => row.map(val => {
      const escaped = String(val).replace(/"/g, '""');
      return `"${escaped}"`;
    }).join(','))
  ].join('\n');

  const blob = new Blob(["\uFEFF" + csvString], { type: 'text/csv;charset=utf-8;' });
  const link = document.createElement('a');
  if (link.download !== undefined) {
    const url = URL.createObjectURL(blob);
    link.setAttribute('href', url);
    link.setAttribute('download', filename);
    link.style.visibility = 'hidden';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  }
};

export const generatePdf = (projects, filename = 'projects.pdf', options = {}) => {
  if (!projects || projects.length === 0) {
    console.warn("generatePdf: No projects to export");
    return;
  }

  const { exporterName = 'System', title = 'Project Report', locale = 'en-CA', sortBy, sortOrder } = options;
  const t = (key, opts) => i18n.t(key, { ...opts, lng: locale.split('-')[0] });
  
  const doc = new jsPDF('p', 'mm', 'a4');
  const isSingle = projects.length === 1;
  const p = projects[0];

  // Header Design
  doc.setFillColor(20, 20, 20);
  doc.rect(0, 0, 210, 28, 'F');
  doc.addImage(logo, 'PNG', 11, 5, 45, 11);
  
  doc.setTextColor(255, 255, 255);
  doc.setFontSize(11);
  doc.setFont("helvetica", "normal");
  doc.text(title, 14, 24);
  
  doc.setFontSize(8);
  doc.text(`${t('pdf.exportedBy')}: ${exporterName}`, 196, 11, { align: 'right' });
  doc.text(`${t('pdf.date')}: ${new Date().toLocaleString(locale, { timeZoneName: 'short' })}`, 196, 15, { align: 'right' });
  doc.text(`${t('pdf.totalProjects')}: ${projects.length}`, 196, 19, { align: 'right' });
  if (sortBy) {
    const orderStr = sortOrder === 'DESC' ? t('pdf.descending') : t('pdf.ascending');
    doc.text(`${t('pdf.sortedBy')}: ${sortBy} (${orderStr})`, 196, 23, { align: 'right' });
  }

  let startTableY = 35;

  // --- Premium Single Project Layout ---
  if (isSingle) {
    doc.setTextColor(40, 40, 40);
    
    // Project Name & ID
    doc.setFontSize(18);
    doc.setFont("helvetica", "bold");
    doc.text(p.name, 14, 45);
    
    doc.setFontSize(10);
    doc.setFont("helvetica", "normal");
    doc.setTextColor(100, 100, 100);
    doc.text(`${t('project.id')}: ${p.projectIdentifier}`, 14, 51);

    // Disclaimer
    doc.setFontSize(7);
    doc.setFont("helvetica", "italic");
    doc.text(t('pdf.disclaimer'), 14, 56);

    // Section 1: Overview
    doc.setDrawColor(230, 230, 230);
    doc.line(14, 62, 196, 62);
    
    doc.setTextColor(20, 20, 20);
    doc.setFontSize(10);
    doc.setFont("helvetica", "bold");
    doc.text(t('pdf.overview'), 14, 70);

    const mappedStatus = p.status === 'IN_PROGRESS' ? 'inProgress' : (p.status?.toLowerCase() || 'none');
    
    const overviewData = [
      [t('project.client'), p.clientName || '-'],
      [t('project.status'), t(`project.${mappedStatus}`)],
      [t('project.priority'), t(`project.priority${p.priority?.charAt(0).toUpperCase() + p.priority?.slice(1).toLowerCase() || 'none'}`)],
      [t('project.projectType'), t(`admin.stats.${p.projectType?.toLowerCase() || 'none'}`)]
    ];

    autoTable(doc, {
      body: overviewData,
      startY: 74,
      margin: { left: 14 },
      theme: 'plain',
      styles: { fontSize: 9, cellPadding: 1 },
      columnStyles: { 0: { fontStyle: 'bold', cellWidth: 35, textColor: [100, 100, 100] } },
    });

    // Section 2: Timeline & Financials
    const nextY = doc.lastAutoTable.finalY + 10;
    doc.setFont("helvetica", "bold");
    doc.text(t('pdf.timeline'), 14, nextY);

    const timelineData = [
      [t('project.startDate'), p.startDate || '-'],
      [t('project.dueDate'), p.dueDate || '-'],
      [t('project.estimatedCost'), formatMoney(p.estimatedCost, p.estimatedCostCurrency, locale)],
      [t('project.addressLabel'), formatAddress(p.address)]
    ];

    autoTable(doc, {
      body: timelineData,
      startY: nextY + 4,
      margin: { left: 14 },
      theme: 'plain',
      styles: { fontSize: 9, cellPadding: 1 },
      columnStyles: { 0: { fontStyle: 'bold', cellWidth: 35, textColor: [100, 100, 100] } },
    });

    // Section 3: Personnel
    const personnelY = doc.lastAutoTable.finalY + 10;
    doc.setFont("helvetica", "bold");
    doc.text(t('pdf.personnel'), 14, personnelY);
    
    doc.setFont("helvetica", "normal");
    doc.setFontSize(9);
    doc.text(formatEmployees(p.assignedEmployeeEmails, '-'), 14, personnelY + 6, { maxWidth: 182 });

    startTableY = personnelY + 20;

    // Line separator before the summary table
    doc.setDrawColor(20, 20, 20);
    doc.setLineWidth(0.5);
    doc.line(14, startTableY - 5, 196, startTableY - 5);
  }

  // --- Table Layout ---
  const tableColumn = [
    t('project.id'), 
    t('project.projectName'), 
    t('project.client'), 
    t('project.employee'), 
    t('project.status'), 
    t('project.priority'), 
    t('project.projectType'), 
    t('project.estimatedCost'), 
    t('project.addressLabel')
  ];
  const tableRows = projects.map(proj => {
    const location = formatAddress(proj.address);
    const mappedStatus = proj.status === 'IN_PROGRESS' ? 'inProgress' : (proj.status?.toLowerCase() || 'none');

    return [
      proj.projectIdentifier || '',
      proj.name || '',
      proj.clientName || '-',
      formatEmployees(proj.assignedEmployeeEmails, '-'),
      t(`project.${mappedStatus}`),
      t(`project.priority${proj.priority?.charAt(0).toUpperCase() + proj.priority?.slice(1).toLowerCase() || 'none'}`),
      t(`admin.stats.${proj.projectType?.toLowerCase() || 'none'}`),
      formatMoney(proj.estimatedCost, proj.estimatedCostCurrency, locale),
      location,
    ];
  });

  autoTable(doc, {
    head: [tableColumn],
    body: tableRows,
    startY: startTableY,
    theme: 'striped',
    headStyles: { 
      fillColor: [20, 20, 20],
      textColor: [255, 255, 255],
      fontSize: 7.5,
      fontStyle: 'bold',
      halign: 'center',
      valign: 'middle',
      minCellHeight: 8
    },
    styles: { fontSize: 7, cellPadding: 1.5, overflow: 'linebreak' },
    columnStyles: {
      0: { fontStyle: 'bold', cellWidth: 12 },
      1: { cellWidth: 35 },
      2: { cellWidth: 24 },
      3: { cellWidth: 30 },
      4: { halign: 'center', cellWidth: 16 },
      5: { halign: 'center', cellWidth: 16 },
      6: { halign: 'center', cellWidth: 15 },
      7: { halign: 'right', cellWidth: 18 },
      8: { cellWidth: 23 },
    },
    didParseCell: function(data) {
      if (data.section === 'body') {
        const rawProject = projects[data.row.index];
        if (!rawProject) return;

        const hexToRgb = (hex) => {
          const r = parseInt(hex.slice(1, 3), 16);
          const g = parseInt(hex.slice(3, 5), 16);
          const b = parseInt(hex.slice(5, 7), 16);
          return [r, g, b];
        };

        if (data.column.index === 0) data.cell.styles.textColor = [50, 50, 50];

        if (data.column.index === 4) {
          const status = rawProject.status;
          if (status === 'COMPLETED') data.cell.styles.textColor = hexToRgb('#047857');
          if (status === 'IN_PROGRESS') data.cell.styles.textColor = hexToRgb('#1d4ed8');
          if (status === 'PENDING') data.cell.styles.textColor = hexToRgb('#b45309');
        }

        if (data.column.index === 5) {
          const priority = rawProject.priority;
          if (priority === 'URGENT') data.cell.styles.textColor = hexToRgb('#991b1b');
          if (priority === 'HIGH') data.cell.styles.textColor = hexToRgb('#dc2626');
          if (priority === 'MEDIUM') data.cell.styles.textColor = hexToRgb('#ca8a04');
          if (priority === 'LOW') data.cell.styles.textColor = hexToRgb('#059669');
        }

        if (data.column.index === 6) data.cell.styles.textColor = [100, 100, 100];
      }
    },
    alternateRowStyles: { fillColor: [245, 245, 245] }
  });

  const totalPagesCount = doc.internal.getNumberOfPages();
  for (let i = 1; i <= totalPagesCount; i++) {
    doc.setPage(i);
    doc.setFontSize(8);
    doc.setTextColor(150);
    const pageText = t('pdf.pageOf', { current: i, total: totalPagesCount });
    doc.text(pageText, 105, 287, { align: 'center' });
    doc.text(`© ${new Date().getFullYear()} VladTech Inc. ${t('pdf.confidential')}`, 14, 287);
  }

  doc.save(filename);
};
