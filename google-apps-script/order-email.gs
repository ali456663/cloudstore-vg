function doPost(e) {
  const data = JSON.parse(e.postData.contents);

  const itemsText = data.items
    .map(function (item) {
      return [
        "Produkt: " + item.productTitle,
        "Produkt ID: " + item.productId,
        "Farg: " + item.selectedColor,
        "Storlek: " + item.selectedSize,
        "Antal: " + item.quantity
      ].join("\n");
    })
    .join("\n\n");

  const message = [
    "Ny bestallning #" + data.orderId,
    "",
    "Kund: " + data.customerName,
    "Email: " + data.customerEmail,
    "Telefon: " + data.customerPhoneNumber,
    "",
    "Produkter:",
    itemsText
  ].join("\n");

  MailApp.sendEmail({
    to: data.sellerEmail || "ali.wafa17943@gmail.com",
    subject: "Ny bestallning #" + data.orderId + " - CloudStore",
    body: message
  });

  return ContentService
    .createTextOutput(JSON.stringify({ status: "ok" }))
    .setMimeType(ContentService.MimeType.JSON);
}
