/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package beatbox;

/**INterface for handling the result of an input dialog
 *
 * @author Kyle
 */
public interface InputHandler {
    /**
     * 
     * @param input the string entered in the dialog.
     */
    public void onInputSent(String input);
}
