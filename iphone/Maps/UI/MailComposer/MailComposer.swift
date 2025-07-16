@objcMembers
final class MailComposer: NSObject {

  private static let mailComposer = MailComposer()
  private static var topViewController: UIViewController { .topViewController() }

  private override init() {}

  /// Composes an email with the provided subject, body and attachment file for the given recipients.
  static func sendEmail(subject: String? = nil, body: String? = nil, toRecipients recipients: [String], attachmentFileURL: URL? = nil) {
    sendEmailWith(subject: subject ?? "",
                  body: body ?? "" ,
                  toRecipients: recipients,
                  attachmentFileURL: attachmentFileURL)
  }

  /// Composes an email with the additional app information and the log file attachment for the developers.
  static func sendBugReportWith(title: String) {
    func subject() -> String {
      let appInfo = AppInfo.shared()
      return String(format:"[%@-%@ iOS] %@", appInfo.bundleVersion, appInfo.buildNumber, title)
    }

    func body() -> String {
      let appInfo = AppInfo.shared()
      return String(format: "\n\n\n\n- %@ (%@)\n- CoMaps %@-%@\n- %@-%@\n- %@\n",
                    appInfo.deviceModel, UIDevice.current.systemVersion,
                    appInfo.bundleVersion, appInfo.buildNumber,
                    Locale.current.languageCode ?? "",
                    Locale.current.regionCode ?? "",
                    Locale.preferredLanguages.joined(separator: ", "))
    }
    UIApplication.shared.showLoadingOverlay {
      let logFileURL = Logger.getLogFileURL()
      UIApplication.shared.hideLoadingOverlay {
        sendEmailWith(subject: subject(),
                      body: body(),
                      toRecipients: [SocialMedia.emailAddress],
                      attachmentFileURL: logFileURL)
      }
    }
  }

  private static func sendEmailWith(subject: String, body: String, toRecipients recipients: [String], attachmentFileURL: URL? = nil) {
    // If the attachment file path is provided, the default mail composer should be used, if possible.
    if let attachmentFileURL, MWMMailViewController.canSendMail(), let attachmentData = try? Data(contentsOf: attachmentFileURL) {
      let mailViewController = MWMMailViewController()
      mailViewController.mailComposeDelegate = mailComposer
      mailViewController.setSubject(subject)
      mailViewController.setMessageBody(body, isHTML:false)
      mailViewController.setToRecipients(recipients)
      mailViewController.addAttachmentData(attachmentData, mimeType: "application/zip", fileName: attachmentFileURL.lastPathComponent)
      topViewController.present(mailViewController, animated: true, completion:nil)
    } else if !openDefaultMailApp(subject: subject, body: body, recipients: recipients) {
      showMailComposingAlert(recipients: recipients)
    }
  }

  private static func openDefaultMailApp(subject: String, body: String, recipients: [String]) -> Bool {
    var components = URLComponents(string: "mailto:\(recipients.joined(separator: ";"))")
    components?.queryItems = [
      URLQueryItem(name: "subject", value: subject),
      URLQueryItem(name: "body", value: body.replacingOccurrences(of: "\n", with: "\r\n")),
    ]

    if let url = components?.url, UIApplication.shared.canOpenURL(url) {
      UIApplication.shared.open(url)
      return true
    }
    return false
  }

  private static func showMailComposingAlert(recipients: [String]) {
    let text = String(format:L("email_error_body"), recipients.joined(separator: ";"))
    let alert = UIAlertController(title: L("email_error_title"), message: text, preferredStyle: .alert)
    let action = UIAlertAction(title: L("ok"), style: .default, handler: nil)
    alert.addAction(action)
    topViewController.present(alert, animated: true, completion: nil)
  }

}

// MARK: - MFMailComposeViewControllerDelegate
extension MailComposer: MFMailComposeViewControllerDelegate {
  func mailComposeController(_ controller: MFMailComposeViewController, didFinishWith result: MFMailComposeResult, error: Error?) {
    controller.dismiss(animated: true, completion: nil)
  }
}
