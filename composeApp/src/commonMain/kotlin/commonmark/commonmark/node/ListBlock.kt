package commonmark.commonmark.node

/**
 * A list block like {@link BulletList} or {@link OrderedList}.
 */
abstract class ListBlock: Block() {

    private var tight: Boolean = false

    /**
     * @return whether this list is tight or loose
     * @see <a href="https://spec.commonmark.org/0.31.2/#tight">CommonMark Spec for tight lists</a>
     */
    fun isTight(): Boolean = tight
    fun setTight(tight: Boolean) {
        this.tight = tight
    }
}